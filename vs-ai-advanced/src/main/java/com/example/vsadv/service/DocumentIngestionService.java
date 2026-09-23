package com.example.vsadv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced document ingestion service supporting:
 * - Multi-source ingestion (different directories, metadata tagging)
 * - Document preprocessing: cleaning, normalization, metadata enrichment
 * - Configurable chunking with overlap
 * - Custom batching strategy for large-scale ingestion
 * - Source-level statistics tracking
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final ResourcePatternResolver resourceResolver;

    @Value("${rag.ingestion.batch-size:50}")
    private int batchSize;

    /** Tracks which sources have been ingested and their chunk counts */
    private final Map<String, Integer> sourceChunkCounts = new ConcurrentHashMap<>();
    private final AtomicInteger totalDocumentsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalChunksCreated = new AtomicInteger(0);

    public DocumentIngestionService(VectorStore vectorStore,
                                     TokenTextSplitter textSplitter,
                                     ResourcePatternResolver resourceResolver) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
        this.resourceResolver = resourceResolver;
    }

    /**
     * Ingests all .txt documents from the classpath:documents/ directory
     * using batch processing.
     */
    public int ingestAllSources() {
        String locationPattern = "classpath:documents/*.txt";
        Resource[] resources;
        try {
            resources = resourceResolver.getResources(locationPattern);
        } catch (Exception e) {
            log.error("Failed to resolve resources from: {}", locationPattern, e);
            throw new RuntimeException("Failed to load documents", e);
        }

        if (resources.length == 0) {
            log.warn("No documents found at {}", locationPattern);
            return 0;
        }

        log.info("Found {} documents to ingest", resources.length);
        AtomicInteger docCount = new AtomicInteger(0);

        // Collect all chunks first, then batch-insert
        List<Document> allChunks = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                List<Document> documentChunks = processDocument(resource);
                allChunks.addAll(documentChunks);
                docCount.incrementAndGet();
                totalDocumentsProcessed.incrementAndGet();
            } catch (Exception e) {
                log.error("Failed to process: {}", resource.getFilename(), e);
            }
        }

        // Batch insert into vector store
        batchInsert(allChunks);

        int count = docCount.get();
        log.info("Ingestion complete. Documents: {}, Total chunks: {}, Sources: {}",
                count, allChunks.size(), sourceChunkCounts.keySet());
        return count;
    }

    /**
     * Processes a single document: read, clean, enrich metadata, and chunk.
     */
    private List<Document> processDocument(Resource resource) throws Exception {
        String source = resource.getFilename();
        log.info("Processing document: {}", source);

        // 1. Read raw document using TextReader
        TextReader reader = new TextReader(resource);
        reader.getCustomMetadata().put("source", source);
        reader.getCustomMetadata().put("ingestionId", UUID.randomUUID().toString());

        // Add source-type specific metadata
        enrichSourceMetadata(reader, source);

        List<Document> rawDocuments = reader.get();

        // 2. Clean text content
        List<Document> cleanedDocuments = new ArrayList<>();
        for (Document doc : rawDocuments) {
            Document cleaned = cleanDocument(doc);
            if (cleaned != null) {
                cleanedDocuments.add(cleaned);
            }
        }

        // 3. Split into chunks using TokenTextSplitter
        List<Document> chunks = textSplitter.apply(cleanedDocuments);

        // 4. Add chunk-level metadata
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            chunk.getMetadata().put("chunkIndex", i);
            chunk.getMetadata().put("totalChunks", chunks.size());
            chunk.getMetadata().put("ingestionTimestamp", System.currentTimeMillis());
            chunk.getMetadata().put("source", source);
        }

        sourceChunkCounts.merge(source, chunks.size(), Integer::sum);
        totalChunksCreated.addAndGet(chunks.size());

        log.info("Document '{}' -> {} chunks", source, chunks.size());
        return chunks;
    }

    /**
     * Cleans document text: removes excessive whitespace, normalizes line breaks,
     * strips control characters while preserving meaningful content.
     */
    private Document cleanDocument(Document document) {
        if (document.getText() == null) return null;

        String cleaned = document.getText()
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll(" {2,}", " ")
                .trim();

        if (cleaned.length() < 10) {
            return null;
        }

        document.setText(cleaned);
        return document;
    }

    /**
     * Adds source-type specific metadata for filtering and reranking.
     */
    private void enrichSourceMetadata(TextReader reader, String source) {
        String lower = source.toLowerCase();
        if (lower.contains("compliance") || lower.contains("policy")) {
            reader.getCustomMetadata().put("documentType", "policy");
            reader.getCustomMetadata().put("authorityLevel", "high");
        } else if (lower.contains("technical") || lower.contains("spec")) {
            reader.getCustomMetadata().put("documentType", "technical");
            reader.getCustomMetadata().put("authorityLevel", "high");
        } else if (lower.contains("product") || lower.contains("catalog")) {
            reader.getCustomMetadata().put("documentType", "product");
            reader.getCustomMetadata().put("authorityLevel", "medium");
        } else if (lower.contains("faq")) {
            reader.getCustomMetadata().put("documentType", "faq");
            reader.getCustomMetadata().put("authorityLevel", "medium");
        } else {
            reader.getCustomMetadata().put("documentType", "general");
            reader.getCustomMetadata().put("authorityLevel", "standard");
        }
    }

    /**
     * Inserts documents into the VectorStore in configurable batch sizes.
     */
    private void batchInsert(List<Document> allChunks) {
        int total = allChunks.size();
        int inserted = 0;

        while (inserted < total) {
            int end = Math.min(inserted + batchSize, total);
            List<Document> batch = allChunks.subList(inserted, end);
            vectorStore.add(batch);
            log.debug("Batch inserted chunks {}-{} of {}", inserted, end, total);
            inserted = end;
        }

        log.info("Batch insertion complete: {} chunks in {} batches",
                total, (total + batchSize - 1) / batchSize);
    }

    public Map<String, Object> getIngestionStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDocuments", totalDocumentsProcessed.get());
        stats.put("totalChunks", totalChunksCreated.get());
        stats.put("sourceCounts", Map.copyOf(sourceChunkCounts));
        stats.put("batchSize", batchSize);
        return stats;
    }
}
