package com.example.vsadv.advisor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

/**
 * Implements query rewriting for improved retrieval performance.
 * Takes an ambiguous user question and rewrites it to better match
 * the vocabulary and structure of the knowledge base documents.
 *
 * Pipeline:
 * 1. Analyze query for ambiguity and missing context
 * 2. Generate a rewritten query optimized for retrieval using the LLM
 * 3. Use the rewritten query for similarity search
 * 4. Pass rewritten question to the LLM for better context matching
 */
public class QueryRewritingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(QueryRewritingAdvisor.class);

    private final ChatClient rewriteClient;
    private final int maxRewrites;
    private final boolean rewritesEnabled;
    private final MeterRegistry meterRegistry;
    private int order = -10; // Run early in the chain

    public QueryRewritingAdvisor(ChatClient rewriteClient,
                                  int maxRewrites,
                                  boolean rewritesEnabled,
                                  MeterRegistry meterRegistry) {
        this.rewriteClient = rewriteClient;
        this.maxRewrites = maxRewrites;
        this.rewritesEnabled = rewritesEnabled;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain chain) {
        String originalQuery = chatClientRequest.userText();

        if (!rewritesEnabled || isSimpleQuery(originalQuery)) {
            log.debug("QueryRewritingAdvisor: Skipping rewrite for simple query");
            return chatClientRequest;
        }

        long startTime = System.currentTimeMillis();

        try {
            String rewrittenQuery = rewriteQuery(originalQuery);

            long elapsed = System.currentTimeMillis() - startTime;
            Timer.builder("rag.query.rewrite.duration")
                    .register(meterRegistry)
                    .record(Duration.ofMillis(elapsed));

            log.info("QueryRewritingAdvisor: '{}' -> '{}'", originalQuery, rewrittenQuery);

            // Return mutated request with original query preserved in prompt
            return chatClientRequest.mutate()
                    .user("Original question: " + originalQuery +
                          "\nOptimized for search: " + rewrittenQuery +
                          "\n\nPlease answer based on the available context.")
                    .build();

        } catch (Exception e) {
            log.warn("QueryRewritingAdvisor: Rewrite failed, using original query", e);
            return chatClientRequest;
        }
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain chain) {
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> aroundStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return Flux.defer(() -> {
            ChatClientRequest modifiedRequest = before(request, null);
            return chain.nextAroundStream(modifiedRequest);
        });
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Uses the LLM to rewrite the query for better retrieval performance.
     * The rewritten query expands acronyms, adds domain-specific terms,
     * and rephrases to match document structure.
     */
    private String rewriteQuery(String originalQuery) {
        String rewritePrompt = String.format("""
            You are a query optimization expert for a corporate knowledge base
            about cloud computing, AI services, security products, and corporate
            policies.

            Your task: Rewrite the following user query to improve document
            retrieval from a vector database. Make these improvements:

            1. Expand abbreviations and acronyms (e.g., "SLA" -> "Service Level Agreement SLA")
            2. Add domain-specific terms that appear in technical documentation
            3. Rephrase vague questions into specific, searchable queries
            4. Include synonyms for key concepts
            5. Keep the rewritten query under 200 characters

            User question: "%s"

            Respond with ONLY the rewritten query text. No explanation, no quotes, no prefixes.
            """, originalQuery);

        String rewritten = rewriteClient.prompt(rewritePrompt).call().content();

        // Clean up the response
        if (rewritten != null) {
            rewritten = rewritten.trim()
                    .replaceAll("^\"|\"$", "")
                    .replaceAll("^Rewritten[:\\s]*", "")
                    .replaceAll("^Optimized[:\\s]*", "")
                    .replaceAll("^Query[:\\s]*", "");

            if (rewritten.length() > 300) {
                rewritten = rewritten.substring(0, 300);
            }
        }

        return rewritten != null && !rewritten.isBlank() ? rewritten : originalQuery;
    }

    /**
     * Determines if a query is simple enough to skip rewriting.
     */
    private boolean isSimpleQuery(String query) {
        if (query.split("\\s+").length <= 3) return true;
        String lower = query.toLowerCase();
        return lower.startsWith("what is ") && lower.length() < 50;
    }
}
