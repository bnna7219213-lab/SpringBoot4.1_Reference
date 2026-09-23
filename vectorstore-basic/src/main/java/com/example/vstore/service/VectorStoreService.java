package com.example.vstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service demonstrating core VectorStore operations:
 * - Document ingestion with automatic text splitting
 * - Similarity search with configurable topK
 *
 * Uses SimpleVectorStore (in-memory) with a local ONNX embedding model.
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final VectorStore vectorStore;

    @Autowired
    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Ingests text documents from the specified location pattern.
     * Each document is split into smaller chunks using TokenTextSplitter,
     * then stored in the vector store.
     *
     * @param locationPattern a Spring resource pattern (e.g., "classpath:documents/*.txt")
     * @return the number of ingested documents
     */
    public int ingestDocuments(String locationPattern) {
        log.info("Ingesting documents from: {}", locationPattern);

        List<Document> allDocuments = new ArrayList<>();

        try {
            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(null)
                    .getResources(locationPattern);

            if (resources.length == 0) {
                // Fallback: try loading from current working directory as a file path
                log.warn("No resources found via classpath pattern, trying file path resolution");
                java.nio.file.Path path = Paths.get(locationPattern.replace("classpath:", ""));
                if (Files.exists(path)) {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("source", path.toString());
                        metadata.put("type", "text");
                        metadata.put("filename", path.getFileName().toString());
                        allDocuments.add(new Document(sb.toString().trim(), metadata));
                    }
                }
            } else {
                for (Resource resource : resources) {
                    String content = new String(resource.getInputStream().readAllBytes());
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", resource.getFilename());
                    metadata.put("type", "text");
                    metadata.put("filename", resource.getFilename());

                    // Split content by paragraphs (double newline) into individual documents
                    String[] paragraphs = content.split("\\n\\n");
                    for (String paragraph : paragraphs) {
                        if (paragraph.trim().length() > 20) {
                            Map<String, Object> chunkMetadata = new HashMap<>(metadata);
                            chunkMetadata.put("chunk", true);
                            allDocuments.add(new Document(paragraph.trim(), chunkMetadata));
                        }
                    }

                    log.info("Loaded resource: {} ({} characters, {} paragraphs parsed)",
                            resource.getFilename(), content.length(), paragraphs.length);
                }
            }

            if (!allDocuments.isEmpty()) {
                // Further chunk large documents using TokenTextSplitter
                TokenTextSplitter splitter = new TokenTextSplitter(200, 50, 5, 1000, true);
                List<Document> chunks = splitter.apply(allDocuments);

                log.info("Total documents after splitting: {}", chunks.size());

                // Add all chunks to the vector store
                vectorStore.add(chunks);
                log.info("Successfully added {} documents to VectorStore", chunks.size());
                return chunks.size();
            } else {
                log.warn("No documents found to ingest from: {}", locationPattern);
                return 0;
            }
        } catch (Exception e) {
            log.error("Error ingesting documents: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest documents from: " + locationPattern, e);
        }
    }

    /**
     * Performs a similarity search against the ingested documents.
     *
     * @param query the search query text
     * @param topK  the number of top results to return
     * @return a list of maps, each containing "score", "content", and "metadata"
     */
    public List<Map<String, Object>> search(String query, int topK) {
        log.info("Searching for: '{}' (topK={})", query, topK);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        var results = vectorStore.similaritySearch(searchRequest);

        List<Map<String, Object>> response = new ArrayList<>();
        if (results != null) {
            for (Document doc : results) {
                Map<String, Object> result = new HashMap<>();
                result.put("content", doc.getText());

                Object score = doc.getMetadata().get("score");
                if (score instanceof Number) {
                    result.put("score", ((Number) score).doubleValue());
                } else {
                    result.put("score", 1.0);
                }

                result.put("metadata", doc.getMetadata());
                response.add(result);
            }

            log.info("Found {} result(s) for query: '{}'", response.size(), query);
        }

        return response;
    }

    /**
     * Performs similarity search with a similarity threshold filter.
     *
     * @param query     the search query text
     * @param topK      the number of top results to return
     * @param threshold the minimum similarity score threshold (0.0 to 1.0)
     * @return a list of maps containing "score", "content", and "metadata"
     */
    public List<Map<String, Object>> searchWithThreshold(String query, int topK, double threshold) {
        log.info("Searching for: '{}' (topK={}, threshold={})", query, topK, threshold);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();

        var results = vectorStore.similaritySearch(searchRequest);

        List<Map<String, Object>> response = new ArrayList<>();
        if (results != null) {
            for (Document doc : results) {
                Map<String, Object> result = new HashMap<>();
                result.put("content", doc.getText());
                Object score = doc.getMetadata().get("score");
                if (score instanceof Number) {
                    result.put("score", ((Number) score).doubleValue());
                } else {
                    result.put("score", 1.0);
                }
                result.put("metadata", doc.getMetadata());
                response.add(result);
            }
        }

        return response;
    }
}
