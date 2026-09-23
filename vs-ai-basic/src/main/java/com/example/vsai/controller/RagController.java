package com.example.vsai.controller;

import com.example.vsai.service.RagService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * POST /api/rag/ask
     * Body: { "question": "What is the return policy?" }
     * Returns the RAG-augmented answer from the LLM.
     */
    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Field 'question' is required and must not be blank"));
        }

        log.info("POST /api/rag/ask - question: {}", question);
        String answer = ragService.askQuestion(question);

        return ResponseEntity.ok(Map.of(
                "question", question,
                "answer", answer,
                "source", "RAG (VectorStore + LLM)"
        ));
    }

    /**
     * POST /api/rag/ingest?dir=documents
     * Triggers re-ingestion of knowledge base documents.
     */
    @PostMapping(value = "/ingest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestKnowledge(
            @RequestParam(defaultValue = "documents") String dir) {
        log.info("POST /api/rag/ingest - directory: {}", dir);
        int ingested = ragService.ingestKnowledge(dir);

        return ResponseEntity.ok(Map.of(
                "directory", dir,
                "documentsIngested", ingested,
                "status", "success"
        ));
    }

    /**
     * GET /api/rag/stats
     * Returns vector store statistics.
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/rag/stats");
        Map<String, Object> stats = ragService.getStats();
        return ResponseEntity.ok(stats);
    }
}
