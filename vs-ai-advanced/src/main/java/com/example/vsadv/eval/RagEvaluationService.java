package com.example.vsadv.eval;

import com.example.vsadv.model.RetrievalResult;
import com.example.vsadv.service.AdvancedRagService;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Evaluation service for measuring RAG pipeline quality.
 *
 * Computes retrieval and answer generation metrics:
 * - Precision@K: How many of the top-K retrieved chunks are relevant
 * - Recall@K: How many relevant chunks appear in top-K
 * - Mean Reciprocal Rank (MRR): Position of first relevant result
 * - Answer Completeness: Whether the answer contains expected terms
 * - Pipeline latency percentiles via Micrometer
 */
@Service
public class RagEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RagEvaluationService.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");

    private final AdvancedRagService ragService;
    private final MeterRegistry meterRegistry;

    public RagEvaluationService(AdvancedRagService ragService,
                                 MeterRegistry meterRegistry) {
        this.ragService = ragService;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Evaluates a batch of questions against expected answer terms.
     * Returns a comprehensive evaluation report with per-query metrics
     * and aggregate statistics.
     */
    public Map<String, Object> evaluateBatch(List<String> questions,
                                              List<List<String>> expectedTerms) {
        log.info("Starting RAG evaluation batch: {} questions", questions.size());

        long batchStart = System.currentTimeMillis();
        List<Map<String, Object>> perQueryResults = new ArrayList<>();

        double totalPrecision = 0;
        double totalRecall = 0;
        double totalMrr = 0;
        double totalCompleteness = 0;
        int validQueries = 0;

        for (int i = 0; i < questions.size(); i++) {
            String question = questions.get(i);
            List<String> expected = (i < expectedTerms.size()) ?
                    expectedTerms.get(i) : List.of();

            try {
                // Run RAG query
                RetrievalResult result = ragService.simpleRagQuery(question);

                // Evaluate retrieval
                RetrievalMetrics retrievalMetrics = evaluateRetrieval(result, expected);

                // Evaluate answer
                double completeness = evaluateAnswerCompleteness(
                        result.getAnswer(), expected);

                Map<String, Object> queryResult = new LinkedHashMap<>();
                queryResult.put("question", question);
                queryResult.put("answer", truncate(result.getAnswer(), 300));
                queryResult.put("precisionAtK", retrievalMetrics.precisionAtK);
                queryResult.put("recallAtK", retrievalMetrics.recallAtK);
                queryResult.put("meanReciprocalRank", retrievalMetrics.mrr);
                queryResult.put("answerCompleteness", completeness);
                queryResult.put("totalTimeMs", result.getTotalTimeMs());
                queryResult.put("chunksRetrieved",
                        result.getRetrievedChunks() != null ? result.getRetrievedChunks().size() : 0);

                perQueryResults.add(queryResult);

                totalPrecision += retrievalMetrics.precisionAtK;
                totalRecall += retrievalMetrics.recallAtK;
                totalMrr += retrievalMetrics.mrr;
                totalCompleteness += completeness;
                validQueries++;

                // Record to Micrometer
                DistributionSummary.builder("rag.eval.precision")
                        .register(meterRegistry)
                        .record(retrievalMetrics.precisionAtK);

            } catch (Exception e) {
                log.error("Evaluation failed for question: {}", question, e);
                Map<String, Object> errorResult = new LinkedHashMap<>();
                errorResult.put("question", question);
                errorResult.put("error", e.getMessage());
                perQueryResults.add(errorResult);
            }
        }

        // Compute aggregates
        long batchTime = System.currentTimeMillis() - batchStart;
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalQueries", questions.size());
        report.put("successfulQueries", validQueries);
        report.put("totalBatchTimeMs", batchTime);

        if (validQueries > 0) {
            report.put("avgPrecision@K", round(totalPrecision / validQueries, 3));
            report.put("avgRecall@K", round(totalRecall / validQueries, 3));
            report.put("avgMRR", round(totalMrr / validQueries, 3));
            report.put("avgAnswerCompleteness", round(totalCompleteness / validQueries, 3));
        }

        report.put("perQueryResults", perQueryResults);

        log.info("RAG evaluation complete: {} queries, {} successful, {}ms total",
                questions.size(), validQueries, batchTime);

        return report;
    }

    /**
     * Evaluates retrieval quality by checking if retrieved chunks
     * contain the expected terms.
     */
    private RetrievalMetrics evaluateRetrieval(RetrievalResult result,
                                                List<String> expectedTerms) {
        List<RetrievalResult.DocumentChunk> chunks = result.getRetrievedChunks();
        if (chunks == null || chunks.isEmpty()) {
            return new RetrievalMetrics(0, 0, 0);
        }

        // Check which chunks are relevant (contain expected terms)
        int k = chunks.size();
        int relevantCount = 0;
        int firstRelevantPosition = -1;

        for (int i = 0; i < chunks.size(); i++) {
            String content = chunks.get(i).getContent();
            if (content != null && containsExpectedTerms(content, expectedTerms)) {
                relevantCount++;
                if (firstRelevantPosition == -1) {
                    firstRelevantPosition = i + 1; // 1-indexed
                }
            }
        }

        double precisionAtK = k > 0 ? (double) relevantCount / k : 0;
        double recallAtK = expectedTerms.isEmpty() ? 0 :
                (double) relevantCount / Math.max(1, expectedTerms.size());
        double mrr = firstRelevantPosition > 0 ? 1.0 / firstRelevantPosition : 0;

        return new RetrievalMetrics(
                round(precisionAtK, 3),
                round(recallAtK, 3),
                round(mrr, 3)
        );
    }

    /**
     * Evaluates answer completeness by checking if expected terms appear
     * in the generated answer.
     */
    private double evaluateAnswerCompleteness(String answer,
                                               List<String> expectedTerms) {
        if (answer == null || answer.isBlank() || expectedTerms.isEmpty()) {
            return 0;
        }

        String answerLower = answer.toLowerCase();
        int matches = 0;

        for (String term : expectedTerms) {
            if (answerLower.contains(term.toLowerCase())) {
                matches++;
            }
        }

        return round((double) matches / expectedTerms.size(), 3);
    }

    /**
     * Checks if content contains any of the expected terms (case-insensitive).
     */
    private boolean containsExpectedTerms(String content, List<String> expectedTerms) {
        String contentLower = content.toLowerCase();
        for (String term : expectedTerms) {
            if (contentLower.contains(term.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    private record RetrievalMetrics(double precisionAtK, double recallAtK, double mrr) {}
}
