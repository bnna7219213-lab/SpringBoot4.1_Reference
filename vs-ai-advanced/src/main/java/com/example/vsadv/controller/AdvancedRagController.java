package com.example.vsadv.controller;

import com.example.vsadv.model.RetrievalResult;
import com.example.vsadv.service.AdvancedRagService;
import com.example.vsadv.service.DocumentIngestionService;
import com.example.vsadv.eval.RagEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/rag")
public class AdvancedRagController {

    private static final Logger log = LoggerFactory.getLogger(AdvancedRagController.class);

    private final AdvancedRagService advancedRagService;
    private final DocumentIngestionService ingestionService;
    private final RagEvaluationService evaluationService;

    public AdvancedRagController(AdvancedRagService advancedRagService,
                                  DocumentIngestionService ingestionService,
                                  RagEvaluationService evaluationService) {
        this.advancedRagService = advancedRagService;
        this.ingestionService = ingestionService;
        this.evaluationService = evaluationService;
    }

    /**
     * POST /api/v2/rag/ask
     * Advanced RAG query with configurable search mode.
     * Body: { "question": "...", "mode": "vector|keyword|hybrid" }
     *
     * Pipeline: rewrite -> retrieve -> rerank -> generate
     * Results are cached for repeat queries.
     */
    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RetrievalResult> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String mode = request.getOrDefault("mode", "vector");

        if (question == null || question.isBlank()) {
            RetrievalResult error = new RetrievalResult();
            error.setAnswer("Field 'question' is required and must not be blank");
            return ResponseEntity.badRequest().body(error);
        }

        log.info("POST /api/v2/rag/ask | mode={} | question={}", mode, question);

        List<String> validModes = List.of("vector", "keyword", "hybrid");
        if (!validModes.contains(mode.toLowerCase())) {
            RetrievalResult error = new RetrievalResult();
            error.setAnswer("Invalid mode. Must be one of: vector, keyword, hybrid");
            return ResponseEntity.badRequest().body(error);
        }

        RetrievalResult result = advancedRagService.advancedRagQuery(question, mode);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v2/rag/ask/simple
     * Simple RAG using Spring AI's built-in QuestionAnswerAdvisor.
     */
    @PostMapping(value = "/ask/simple", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RetrievalResult> askSimple(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            RetrievalResult error = new RetrievalResult();
            error.setAnswer("Field 'question' is required");
            return ResponseEntity.badRequest().body(error);
        }

        log.info("POST /api/v2/rag/ask/simple | question={}", question);
        RetrievalResult result = advancedRagService.simpleRagQuery(question);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v2/rag/ingest
     * Re-ingests all documents from the knowledge base.
     */
    @PostMapping(value = "/ingest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestDocuments() {
        log.info("POST /api/v2/rag/ingest");
        int ingested = ingestionService.ingestAllSources();
        return ResponseEntity.ok(Map.of(
                "documentsIngested", ingested,
                "status", "success"
        ));
    }

    /**
     * GET /api/v2/rag/stats
     * Ingestion and retrieval statistics.
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/v2/rag/stats");
        return ResponseEntity.ok(ingestionService.getIngestionStats());
    }

    /**
     * POST /api/v2/rag/evaluate
     * Runs the evaluation pipeline against a set of test questions.
     * Body: { "questions": ["q1", "q2"], "expectedTerms": [["exp1", "exp2"], [...]] }
     */
    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> evaluateRag(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) request.getOrDefault("questions", List.of());
        @SuppressWarnings("unchecked")
        List<List<String>> expectedTerms = (List<List<String>>) request.getOrDefault("expectedTerms", List.of());

        if (questions.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Field 'questions' must be a non-empty list"));
        }

        log.info("POST /api/v2/rag/evaluate | {} questions", questions.size());
        Map<String, Object> evaluation = evaluationService.evaluateBatch(questions, expectedTerms);
        return ResponseEntity.ok(evaluation);
    }

    /**
     * GET /api/v2/rag/evaluate/demo
     * Runs a predefined evaluation with sample questions.
     */
    @GetMapping(value = "/evaluate/demo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> runDemoEvaluation() {
        log.info("GET /api/v2/rag/evaluate/demo");

        List<String> questions = List.of(
                "What is the SLA uptime guarantee for Enterprise tier?",
                "How do I upgrade from Starter to Professional?",
                "What certifications does ACME maintain?",
                "What is the refund processing time?",
                "Does ACME support multi-region deployment?"
        );

        List<List<String>> expectedTerms = List.of(
                List.of("99.99", "Enterprise", "uptime"),
                List.of("upgrade", "Starter", "Professional"),
                List.of("SOC 2", "GDPR", "HIPAA", "ISO"),
                List.of("5-10", "refund", "business days"),
                List.of("multi-region", "deployment", "Enterprise")
        );

        Map<String, Object> evaluation = evaluationService.evaluateBatch(questions, expectedTerms);
        return ResponseEntity.ok(evaluation);
    }
}
