package com.example.vadv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for VectorStore operations.
 * Provides HTTP endpoints for document ingestion, search, and management.
 */
@RestController
@RequestMapping("/api/v1/vectors")
public class VectorStoreController {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreController.class);

    private final AdvancedVectorStoreService vectorStoreService;

    @Autowired
    public VectorStoreController(AdvancedVectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        var info = new HashMap<String, Object>();
        info.put("status", "ok");
        info.put("service", "AdvancedVectorStoreService");
        info.put("storeInfo", vectorStoreService.getVectorStoreInfo());
        info.put("timestamp", java.time.Instant.now().toString());
        return info;
    }

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double threshold) {

        log.info("REST search: query='{}', topK={}, category={}, author={}, threshold={}",
                query, topK, category, author, threshold);

        List<Map<String, Object>> results;

        if (threshold != null) {
            results = vectorStoreService.searchWithThreshold(query, topK, threshold);
        } else if (category != null || author != null) {
            results = vectorStoreService.searchWithFilters(query, topK, category, author);
        } else {
            results = vectorStoreService.search(query, topK);
        }

        var response = new HashMap<String, Object>();
        response.put("query", query);
        response.put("results", results);
        response.put("totalResults", results.size());
        return response;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody IngestRequest request) {
        log.info("REST ingest: category={}, author={}", request.category(), request.author());

        int count = vectorStoreService.ingestWithMetadata(
                request.locationPattern(),
                request.category(),
                request.author(),
                request.tags() != null ? request.tags() : new String[0]
        );

        var response = new HashMap<String, Object>();
        response.put("status", "ingested");
        response.put("documentsIngested", count);
        response.put("category", request.category());
        return response;
    }

    @DeleteMapping
    public Map<String, Object> delete(@RequestBody List<String> documentIds) {
        boolean success = vectorStoreService.deleteDocuments(documentIds);

        var response = new HashMap<String, Object>();
        response.put("status", success ? "deleted" : "failed");
        response.put("documentsDeleted", documentIds.size());
        return response;
    }

    public record IngestRequest(String locationPattern, String category, String author, String[] tags) {}
}
