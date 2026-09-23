package com.example.vsadv.advisor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom advisor that performs reranking on the top-K results from the
 * initial vector similarity search. Reranking uses a multi-signal scoring
 * approach: it re-evaluates each candidate document by computing
 * a relevance score that combines multiple signals:
 *
 * 1. Cosine similarity approximation (from token overlap)
 * 2. Term overlap score (BM25-inspired keyword matching)
 * 3. Source quality weighting (prioritize authoritative documents)
 *
 * The advisor retrieves candidates from the VectorStore, reranks them,
 * and injects the top results as augmented context into the prompt
 * before sending to the LLM.
 */
public class RerankingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(RerankingAdvisor.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final int rerankTopK;
    private final double similarityThreshold;
    private final MeterRegistry meterRegistry;

    private int order = 0;

    public RerankingAdvisor(VectorStore vectorStore,
                            EmbeddingModel embeddingModel,
                            int rerankTopK,
                            double similarityThreshold,
                            MeterRegistry meterRegistry) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.rerankTopK = rerankTopK;
        this.similarityThreshold = similarityThreshold;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain chain) {
        String userMessage = chatClientRequest.userText();
        log.debug("RerankingAdvisor: Processing query: {}", userMessage);

        long startTime = System.currentTimeMillis();

        // Retrieve candidates from vector store for reranking
        SearchRequest searchRequest = SearchRequest.defaults()
                .withQuery(userMessage)
                .withTopK(rerankTopK * 2)
                .withSimilarityThreshold(0.1); // Low threshold for candidate retrieval

        List<Document> candidates;
        try {
            candidates = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("RerankingAdvisor: Vector search failed, bypassing reranking", e);
            return chatClientRequest;
        }

        if (candidates == null || candidates.isEmpty()) {
            log.debug("RerankingAdvisor: No candidates found for reranking");
            return chatClientRequest;
        }

        // Score and rerank
        List<ScoredDocument> scored = scoreCandidates(userMessage, candidates);
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        // Take top-K after reranking, applying threshold
        List<Document> reranked = scored.stream()
                .filter(sd -> sd.score >= similarityThreshold)
                .limit(rerankTopK)
                .map(sd -> sd.document)
                .toList();

        long elapsed = System.currentTimeMillis() - startTime;
        Timer.builder("rag.reranking.duration")
                .tag("rag.phase", "reranking")
                .register(meterRegistry)
                .record(Duration.ofMillis(elapsed));

        meterRegistry.counter("rag.reranking.candidates", "rag.phase", "reranking")
                .increment(candidates.size());

        log.debug("RerankingAdvisor: Reranked {} candidates to {} in {}ms",
                candidates.size(), reranked.size(), elapsed);

        // Build augmented context and mutate the request
        if (!reranked.isEmpty()) {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("[ADVANCED RERANKED CONTEXT - ").append(reranked.size()).append(" chunks]\n\n");
            for (int i = 0; i < reranked.size(); i++) {
                Document doc = reranked.get(i);
                contextBuilder.append("--- Chunk ").append(i + 1).append(" ---\n");
                contextBuilder.append("Source: ").append(doc.getMetadata().getOrDefault("source", "unknown")).append("\n");
                contextBuilder.append("Content: ").append(doc.getText()).append("\n\n");
            }

            String augmentedPrompt = contextBuilder.toString() + "\n[USER QUESTION]: " + userMessage;

            return chatClientRequest.mutate()
                    .user(augmentedPrompt)
                    .build();
        }

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain chain) {
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> aroundStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // For streaming, apply reranking in the before phase then delegate
        return Flux.defer(() -> {
            ChatClientRequest modifiedRequest = before(request, null);
            return chain.nextAroundStream(modifiedRequest);
        });
    }

    @Override
    public int getOrder() {
        return order;
    }

    public RerankingAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }

    /**
     * Scores each candidate document against the query using multiple signals.
     */
    private List<ScoredDocument> scoreCandidates(String query, List<Document> candidates) {
        Set<String> queryTerms = tokenize(query);

        List<ScoredDocument> results = new ArrayList<>();
        for (Document doc : candidates) {
            String text = doc.getText();
            if (text == null) continue;

            // Signal 1: Vector similarity approximation via token overlap
            double vectorScore = computeSimilarityScore(query, text);

            // Signal 2: Keyword overlap (BM25-inspired)
            double keywordScore = computeKeywordScore(queryTerms, tokenize(text));

            // Signal 3: Source quality weight
            double sourceWeight = getSourceWeight(doc);

            // Combined rerank score
            double rerankScore = (vectorScore * 0.6) + (keywordScore * 0.2) + (sourceWeight * 0.2);

            ScoredDocument sd = new ScoredDocument();
            sd.document = doc;
            sd.score = rerankScore;
            results.add(sd);
        }

        return results;
    }

    /**
     * Approximates cosine similarity using normalized token overlap.
     */
    private double computeSimilarityScore(String query, String text) {
        Set<String> queryTokens = tokenize(query);
        Set<String> docTokens = tokenize(text);

        if (queryTokens.isEmpty() || docTokens.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(docTokens);

        double dotProduct = intersection.size();
        double queryMagnitude = Math.sqrt(queryTokens.size());
        double docMagnitude = Math.sqrt(docTokens.size());

        if (queryMagnitude == 0 || docMagnitude == 0) return 0.0;
        return dotProduct / (queryMagnitude * docMagnitude);
    }

    /**
     * Computes BM25-inspired keyword matching score.
     */
    private double computeKeywordScore(Set<String> queryTerms, Set<String> docTerms) {
        if (queryTerms.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(queryTerms);
        intersection.retainAll(docTerms);

        return (double) intersection.size() / queryTerms.size();
    }

    /**
     * Assigns a quality weight based on the document source.
     */
    private double getSourceWeight(Document doc) {
        String source = (String) doc.getMetadata().getOrDefault("source", "");
        if (source.contains("compliance") || source.contains("policy")) return 0.9;
        if (source.contains("technical") || source.contains("spec")) return 0.8;
        if (source.contains("product") || source.contains("faq")) return 0.7;
        return 0.5;
    }

    /**
     * Tokenizes text into lowercase word tokens.
     */
    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        var matcher = WORD_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static class ScoredDocument {
        Document document;
        double score;
    }
}
