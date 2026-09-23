package com.example.vadv.service;

import com.example.vadv.document.DocumentUploader;
import com.example.vadv.model.DocumentMetadata;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Vector Store Service demonstrating:
 * - Document ingestion with rich metadata (source, category, date, author)
 * - Similarity search with metadata filtering
 * - Threshold-based search for quality control
 * - Document update/delete operations
 * - REST API support
 * - @Observation integration for observability (via @Observed)
 * - Batching strategy awareness
 */
@Service
public class AdvancedVectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(AdvancedVectorStoreService.class);

    private final VectorStore vectorStore;
    private final DocumentUploader documentUploader;

    @Value("\")
    private String vectorStoreType;

    @Autowired
    public AdvancedVectorStoreService(VectorStore vectorStore, DocumentUploader documentUploader) {
        this.vectorStore = vectorStore;
        this.documentUploader = documentUploader;
    }

    /**
     * Ingests documents from a classpath pattern with rich metadata.
     *
     * @param locationPattern resource pattern
     * @param category        document category
     * @param author          document author
     * @param tags            additional tags
     * @return number of documents ingested
     */
    @Observed(name = "vectorstore.ingest", contextualName = "advanced-ingest")
    public int ingestWithMetadata(String locationPattern, String category, String author, String... tags) {
        log.info("Ingesting with metadata [category={}, author={}, tags={}]", category, author, Arrays.toString(tags));

        List<Document> documents = documentUploader.uploadDocuments(locationPattern, category, author, tags);
        vectorStore.add(documents);

        log.info("Successfully ingested {} documents into {} vector store", documents.size(), vectorStoreType);
        return documents.size();
    }

    /**
     * Ingests documents with a DocumentMetadata record.
     */
    @Observed(name = "vectorstore.ingest", contextualName = "ingest-with-record")
    public int ingestWithMetadata(String locationPattern, DocumentMetadata metadata) {
        return ingestWithMetadata(locationPattern, metadata.category(), metadata.author(), metadata.tags());
    }

    /**
     * Performs similarity search with full SearchRequest configuration.
     *
     * Should be called after searchWithFilters.
     *
     * @param query the search query
     * @param topK  top K results
     * @return list of result maps
     */
    @Observed(name = "vectorstore.search", contextualName = "similarity-search-basic")
    public List<Map<String, Object>> search(String query, int topK) {
        log.info("Basic search: '{}' (topK={})", query, topK);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        return executeSearch(request);
    }

    /**
     * Performs similarity search with a similarity threshold.
     * Only results above the threshold are returned.
     *
     * @param query     the search query
     * @param topK      top K results to consider
     * @param threshold minimum similarity score (0.0 to 1.0)
     * @return list of result maps
     */
    @Observed(name = "vectorstore.search", contextualName = "threshold-search")
    public List<Map<String, Object>> searchWithThreshold(String query, int topK, double threshold) {
        log.info("Threshold search: '{}' (topK={}, threshold={})", query, topK, threshold);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();

        return executeSearch(request);
    }

    /**
     * Performs similarity search with metadata filtering.
     *
     * Filters are applied using FilterExpressionBuilder to match only documents
     * with specific metadata values (e.g., category = "research").
     *
     * @param query    the search query
     * @param topK     top K results
     * @param category category filter (nullable)
     * @param author   author filter (nullable)
     * @return list of filtered result maps
     */
    @Observed(name = "vectorstore.search", contextualName = "filtered-search")
    public List<Map<String, Object>> searchWithFilters(String query, int topK,
            String category, String author) {
        log.info("Filtered search: '{}' (topK={}, category={}, author={})", query, topK, category, author);

        var filterBuilder = new org.springframework.ai.vectorstore.filter.FilterExpressionBuilder();

        var filterExpression = filterBuilder.and(
                filterBuilder.or(
                        filterBuilder.eq("category", category != null ? category : ""),
                        filterBuilder.eq("category", "")
                ),
                filterBuilder.or(
                        filterBuilder.eq("author", author != null ? author : ""),
                        filterBuilder.eq("author", "")
                )
        );

        if (category != null && !category.isEmpty()) {
            filterExpression = filterBuilder.eq("category", category);
        }
        if (author != null && !author.isEmpty()) {
            if (filterExpression != null) {
                filterExpression = filterBuilder.and(filterExpression, filterBuilder.eq("author", author));
            } else {
                filterExpression = filterBuilder.eq("author", author);
            }
        }

        SearchRequest.SearchRequestBuilder builder = SearchRequest.builder()
                .query(query)
                .topK(topK);

        if (filterExpression != null) {
            builder.filterExpression(filterExpression);
        }

        return executeSearch(builder.build());
    }

    /**
     * Deletes a document by its ID.
     *
     * @param documentId the unique document ID
     * @return true if deletion was successful
     */
    @Observed(name = "vectorstore.delete", contextualName = "delete-document")
    public boolean deleteDocument(String documentId) {
        try {
            List<String> ids = List.of(documentId);
            vectorStore.delete(ids);
            log.info("Deleted document: {}", documentId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting document {}: {}", documentId, e.getMessage());
            return false;
        }
    }

    /**
     * Deletes documents matching the given IDs.
     *
     * @param documentIds list of document IDs to delete
     * @return true if deletion was successful
     */
    @Observed(name = "vectorstore.delete", contextualName = "batch-delete")
    public boolean deleteDocuments(List<String> documentIds) {
        try {
            vectorStore.delete(documentIds);
            log.info("Deleted {} documents", documentIds.size());
            return true;
        } catch (Exception e) {
            log.error("Error deleting documents: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Executes search and maps results to a standard response format.
     */
    private List<Map<String, Object>> executeSearch(SearchRequest request) {
        var results = vectorStore.similaritySearch(request);

        if (results == null) {
            log.warn("VectorStore returned null for search query");
            return List.of();
        }

        return results.stream().map(doc -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("content", doc.getText());
            entry.put("metadata", doc.getMetadata().isEmpty() ? Map.of() : doc.getMetadata());

            Object score = doc.getMetadata().get("score");
            if (score instanceof Number number) {
                entry.put("score", number.doubleValue());
            } else {
                entry.put("score", 1.0);
            }

            Object id = doc.getMetadata().get("id");
            if (id instanceof UUID uuid) {
                entry.put("id", uuid.toString());
            }
            return entry;
        }).collect(Collectors.toList());
    }

    /**
     * Returns information about the configured vector store instance.
     *
     * @return map of vector store configuration info
     */
    public Map<String, String> getVectorStoreInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("type", vectorStoreType);
        info.put("implementation", vectorStore.getClass().getSimpleName());
        boolean hasFilterSupport = vectorStore instanceof org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
        info.put("filterSupport", String.valueOf(true));
        return info;
    }
}
