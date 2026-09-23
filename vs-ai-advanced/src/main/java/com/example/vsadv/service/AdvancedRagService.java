package com.example.vsadv.service;

import com.example.vsadv.advisor.QueryRewritingAdvisor;
import com.example.vsadv.advisor.RerankingAdvisor;
import com.example.vsadv.model.RetrievalResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Production-grade RAG service with:
 * - Query rewriting for better retrieval
 * - Reranking of results using multi-signal scoring
 * - Hybrid search (vector + keyword)
 * - Caching for repeated queries
 * - Integrated observability via Micrometer timers and counters
 */
@Service
public class AdvancedRagService {

    private static final Logger log = LoggerFactory.getLogger(AdvancedRagService.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w{2,}\\b");

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final RerankingAdvisor rerankingAdvisor;
    private final QueryRewritingAdvisor queryRewritingAdvisor;
    private final MeterRegistry meterRegistry;

    @Value("${rag.search.top-k:10}")
    private int topK;

    @Value("${rag.search.rerank-top-k:5}")
    private int rerankTopK;

    @Value("${rag.search.similarity-threshold:0.65}")
    private double similarityThreshold;

    @Value("${rag.hybrid.vector-weight:0.7}")
    private double vectorWeight;

    @Value("${rag.hybrid.keyword-weight:0.3}")
    private double keywordWeight;

    public AdvancedRagService(VectorStore vectorStore,
                               ChatClient chatClient,
                               QuestionAnswerAdvisor questionAnswerAdvisor,
                               RerankingAdvisor rerankingAdvisor,
                               QueryRewritingAdvisor queryRewritingAdvisor,
                               MeterRegistry meterRegistry) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.questionAnswerAdvisor = questionAnswerAdvisor;
        this.rerankingAdvisor = rerankingAdvisor;
        this.queryRewritingAdvisor = queryRewritingAdvisor;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Executes a full RAG pipeline: rewrite -> retrieve -> rerank -> generate.
     * Results are cached based on the question text.
     */
    @Cacheable(value = "ragQueries", key = "#question.toLowerCase().trim() + '-' + #mode")
    public RetrievalResult advancedRagQuery(String question, String mode) {
        long pipelineStart = System.currentTimeMillis();
        log.info("Advanced RAG pipeline started | mode={} | question={}", mode, question);

        RetrievalResult result = new RetrievalResult();
        result.setQuestion(question);
        result.setSearchMode(mode);
        result.setFromCache(false);

        try {
            // Phase 1: Query Rewriting
            long rewriteStart = System.currentTimeMillis();
            String rewrittenQuestion = rewriteQuestion(question);
            result.setRewrittenQuestion(rewrittenQuestion);
            long rewriteTime = System.currentTimeMillis() - rewriteStart;

            Timer.builder("rag.phase.rewrite").register(meterRegistry)
                    .record(Duration.ofMillis(rewriteTime));

            // Phase 2: Retrieval (vector, keyword, or hybrid)
            long retrievalStart = System.currentTimeMillis();
            List<RetrievalResult.DocumentChunk> chunks;
            switch (mode.toLowerCase()) {
                case "keyword" -> chunks = performKeywordSearch(rewrittenQuestion);
                case "hybrid" -> chunks = performHybridSearch(rewrittenQuestion);
                default -> chunks = performVectorSearch(rewrittenQuestion);
            }
            result.setRetrievedChunks(chunks);
            long retrievalTime = System.currentTimeMillis() - retrievalStart;
            result.setRetrievalTimeMs(retrievalTime);

            Timer.builder("rag.phase.retrieval").tag("mode", mode).register(meterRegistry)
                    .record(Duration.ofMillis(retrievalTime));

            // Phase 3: Reranking
            long rerankStart = System.currentTimeMillis();
            List<RetrievalResult.DocumentChunk> rerankedChunks = rerankResults(
                    rewrittenQuestion, chunks);
            result.setRerankedChunks(rerankedChunks);
            long rerankTime = System.currentTimeMillis() - rerankStart;
            result.setRerankingTimeMs(rerankTime);

            Timer.builder("rag.phase.reranking").register(meterRegistry)
                    .record(Duration.ofMillis(rerankTime));

            // Phase 4: Generation
            long genStart = System.currentTimeMillis();
            String answer = generateAnswer(question, rerankedChunks);
            result.setAnswer(answer);
            long genTime = System.currentTimeMillis() - genStart;
            result.setGenerationTimeMs(genTime);

            Timer.builder("rag.phase.generation").register(meterRegistry)
                    .record(Duration.ofMillis(genTime));

            // Record success metric
            Counter.builder("rag.query.success").register(meterRegistry).increment();

        } catch (Exception e) {
            log.error("RAG pipeline failed for question: {}", question, e);
            result.setAnswer("An error occurred while processing your query. Please try again.");
            Counter.builder("rag.query.failure").register(meterRegistry).increment();
        }

        long totalTime = System.currentTimeMillis() - pipelineStart;
        result.setTotalTimeMs(totalTime);

        Timer.builder("rag.pipeline.total").register(meterRegistry)
                .record(Duration.ofMillis(totalTime));

        log.info("Advanced RAG pipeline completed in {}ms", totalTime);
        return result;
    }

    /**
     * Simple RAG query with default settings (vector search + reranking).
     */
    public RetrievalResult simpleRagQuery(String question) {
        long start = System.currentTimeMillis();
        log.info("Simple RAG query: {}", question);

        // Use QuestionAnswerAdvisor from Spring AI directly
        String answer = chatClient.prompt()
                .user(question)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        RetrievalResult result = new RetrievalResult();
        result.setQuestion(question);
        result.setAnswer(answer);
        result.setSearchMode("vector");
        result.setTotalTimeMs(System.currentTimeMillis() - start);

        return result;
    }

    /**
     * Vector similarity search returning typed DocumentChunks.
     */
    private List<RetrievalResult.DocumentChunk> performVectorSearch(String query) {
        SearchRequest searchRequest = SearchRequest.defaults()
                .withQuery(query)
                .withTopK(topK * 2)
                .withSimilarityThreshold(similarityThreshold);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        List<RetrievalResult.DocumentChunk> chunks = new ArrayList<>();
        int index = 0;
        for (Document doc : documents) {
            RetrievalResult.DocumentChunk chunk = new RetrievalResult.DocumentChunk();
            chunk.setId(UUID.randomUUID().toString());
            chunk.setContent(doc.getText());
            chunk.setSource((String) doc.getMetadata().getOrDefault("source", "unknown"));
            chunk.setChunkIndex(index++);
            chunk.setMetadata(doc.getMetadata());
            // Simple token overlap score as approximation
            chunk.setVectorScore(computeSimpleScore(query, doc.getText()));
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Keyword-based search using term matching.
     */
    private List<RetrievalResult.DocumentChunk> performKeywordSearch(String query) {
        Set<String> queryTerms = tokenize(query);
        SearchRequest searchRequest = SearchRequest.defaults()
                .withQuery(query)
                .withTopK(topK);

        List<Document> candidates = vectorStore.similaritySearch(searchRequest);

        List<RetrievalResult.DocumentChunk> chunks = new ArrayList<>();
        int index = 0;
        for (Document doc : candidates) {
            Set<String> docTerms = tokenize(doc.getText());
            Set<String> intersection = new HashSet<>(queryTerms);
            intersection.retainAll(docTerms);

            double keywordScore = queryTerms.isEmpty() ? 0 :
                    (double) intersection.size() / queryTerms.size();

            if (keywordScore > 0.05) {
                RetrievalResult.DocumentChunk chunk = new RetrievalResult.DocumentChunk();
                chunk.setId(UUID.randomUUID().toString());
                chunk.setContent(doc.getText());
                chunk.setSource((String) doc.getMetadata().getOrDefault("source", "unknown"));
                chunk.setChunkIndex(index++);
                chunk.setKeywordScore(keywordScore);
                chunk.setMetadata(doc.getMetadata());
                chunks.add(chunk);
            }
        }

        chunks.sort((a, b) -> Double.compare(b.getKeywordScore(), a.getKeywordScore()));
        return chunks.stream().limit(rerankTopK).toList();
    }

    /**
     * Hybrid search combining vector and keyword results.
     */
    private List<RetrievalResult.DocumentChunk> performHybridSearch(String query) {
        // Get both sets of results
        List<RetrievalResult.DocumentChunk> vectorResults = performVectorSearch(query);
        List<RetrievalResult.DocumentChunk> keywordResults = performKeywordSearch(query);

        // Merge and deduplicate by content similarity
        Map<String, RetrievalResult.DocumentChunk> merged = new LinkedHashMap<>();

        for (RetrievalResult.DocumentChunk chunk : vectorResults) {
            String key = Integer.toString(chunk.getContent().hashCode());
            RetrievalResult.DocumentChunk copy = new RetrievalResult.DocumentChunk();
            copy.setId(chunk.getId());
            copy.setContent(chunk.getContent());
            copy.setSource(chunk.getSource());
            copy.setVectorScore(chunk.getVectorScore());
            copy.setChunkIndex(chunk.getChunkIndex());
            copy.setMetadata(chunk.getMetadata());
            merged.put(key, copy);
        }

        for (RetrievalResult.DocumentChunk chunk : keywordResults) {
            String key = Integer.toString(chunk.getContent().hashCode());
            RetrievalResult.DocumentChunk existing = merged.get(key);
            if (existing != null) {
                existing.setKeywordScore(chunk.getKeywordScore());
            } else {
                merged.put(key, chunk);
            }
        }

        // Compute hybrid scores
        List<RetrievalResult.DocumentChunk> results = new ArrayList<>(merged.values());
        for (RetrievalResult.DocumentChunk chunk : results) {
            double vScore = chunk.getVectorScore();
            double kScore = chunk.getKeywordScore();
            double hybrid = (vScore * vectorWeight) + (kScore * keywordWeight);
            chunk.setHybridScore(hybrid);
        }

        // Sort by hybrid score and return top-K
        results.sort((a, b) -> Double.compare(b.getHybridScore(), a.getHybridScore()));
        return results.stream().limit(rerankTopK).toList();
    }

    /**
     * Reranks retrieval results using multi-signal scoring.
     */
    private List<RetrievalResult.DocumentChunk> rerankResults(String query,
                                                               List<RetrievalResult.DocumentChunk> chunks) {
        Set<String> queryTerms = tokenize(query);

        for (RetrievalResult.DocumentChunk chunk : chunks) {
            double vectorScore = chunk.getVectorScore();
            double keywordScore = computeKeywordScore(queryTerms, tokenize(chunk.getContent()));
            double sourceWeight = getSourceWeight(chunk.getSource());

            // Combined rerank score
            double rerankScore = (vectorScore * 0.5) + (keywordScore * 0.3) + (sourceWeight * 0.2);
            chunk.setRerankScore(rerankScore);
        }

        chunks.sort((a, b) -> Double.compare(b.getRerankScore(), a.getRerankScore()));
        return chunks.stream().limit(rerankTopK).toList();
    }

    /**
     * Generates the final answer using the ChatClient with augmented context.
     */
    private String generateAnswer(String question,
                                   List<RetrievalResult.DocumentChunk> rerankedChunks) {
        if (rerankedChunks.isEmpty()) {
            return "The knowledge base does not contain sufficient information to answer this question.";
        }

        // Build context from reranked chunks
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("[CONTEXT - ").append(rerankedChunks.size()).append(" relevant documents]\n\n");

        for (int i = 0; i < rerankedChunks.size(); i++) {
            RetrievalResult.DocumentChunk chunk = rerankedChunks.get(i);
            contextBuilder.append("--- Document ").append(i + 1).append(" ---\n");
            contextBuilder.append("Source: ").append(chunk.getSource()).append("\n");
            contextBuilder.append("Relevance: ").append(String.format("%.2f", chunk.getRerankScore())).append("\n\n");
            contextBuilder.append(chunk.getContent()).append("\n\n");
        }

        contextBuilder.append("\n[INSTRUCTIONS]\n");
        contextBuilder.append("Using ONLY the context above, answer this question concisely:\n");
        contextBuilder.append(question);

        return chatClient.prompt()
                .user(contextBuilder.toString())
                .call()
                .content();
    }

    /**
     * Rewrites the query for better retrieval.
     */
    private String rewriteQuestion(String original) {
        // For speed, use a lightweight heuristic rewrite
        String rewritten = original;

        // Expand common abbreviations in the cloud domain
        rewritten = rewritten.replaceAll("\\bSLA\\b", "Service Level Agreement SLA");
        rewritten = rewritten.replaceAll("\\bAPI\\b", "Application Programming Interface API");
        rewritten = rewritten.replaceAll("\\bGDPR\\b", "General Data Protection Regulation GDPR");
        rewritten = rewritten.replaceAll("\\bSOC\\b", "Service Organization Control SOC");
        rewritten = rewritten.replaceAll("\\bBYOK\\b", "Bring Your Own Key BYOK");
        rewritten = rewritten.replaceAll("\\bWAF\\b", "Web Application Firewall WAF");
        rewritten = rewritten.replaceAll("\\bCDN\\b", "Content Delivery Network CDN");
        rewritten = rewritten.replaceAll("\\bGPU\\b", "Graphics Processing Unit GPU");

        log.debug("Query rewrite: '{}' -> '{}'", original, rewritten);
        return rewritten;
    }

    // --- Utility methods ---

    private double computeSimpleScore(String query, String text) {
        Set<String> queryTerms = tokenize(query);
        Set<String> docTerms = tokenize(text);
        if (queryTerms.isEmpty() || docTerms.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(queryTerms);
        intersection.retainAll(docTerms);
        return (double) intersection.size() / queryTerms.size();
    }

    private double computeKeywordScore(Set<String> queryTerms, Set<String> docTerms) {
        if (queryTerms.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(queryTerms);
        intersection.retainAll(docTerms);
        return (double) intersection.size() / queryTerms.size();
    }

    private double getSourceWeight(String source) {
        if (source == null) return 0.5;
        String lower = source.toLowerCase();
        if (lower.contains("compliance") || lower.contains("policy")) return 0.9;
        if (lower.contains("technical") || lower.contains("spec")) return 0.8;
        if (lower.contains("api") || lower.contains("reference")) return 0.75;
        return 0.5;
    }

    private Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> tokens = new HashSet<>();
        var matcher = WORD_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
}
