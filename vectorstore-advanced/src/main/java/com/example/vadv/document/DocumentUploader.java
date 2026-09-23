package com.example.vadv.document;

import com.example.vadv.model.DocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles advanced document upload with rich metadata.
 *
 * Features:
 * - Reads documents from classpath file patterns
 * - Attaches structured metadata (category, author, date, tags)
 * - Splits content into manageable chunks
 * - Returns ready-to-ingest Document list for VectorStore
 */
@Component
public class DocumentUploader {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploader.class);

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * Uploads all documents from the given classpath pattern with metadata enrichment.
     *
     * @param locationPattern Spring resource pattern (e.g., "classpath:documents/*.txt")
     * @param category        the category to assign to all documents from this source
     * @param author          the author name
     * @param tags            additional tags
     * @return list of Document objects ready for VectorStore.add()
     */
    public List<Document> uploadDocuments(String locationPattern, String category, String author, String... tags) {
        log.info("Uploading documents from: {} (category={}, author={})", locationPattern, category, author);

        List<Document> documents = new ArrayList<>();

        try {
            Resource[] resources = resourceResolver.getResources(locationPattern);

            for (Resource resource : resources) {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF8);
                String filename = resource.getFilename();
                LocalDate now = LocalDate.now();

                // Create chunked documents with metadata
                String[] paragraphs = content.split("\\n\\n");
                for (int i = 0; i < paragraphs.length; i++) {
                    String paragraph = paragraphs[i].trim();
                    if (paragraph.length() < 20) {
                        continue;
                    }

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", filename);
                    metadata.put("category", category);
                    metadata.put("author", author);
                    metadata.put("date", now.toString());
                    metadata.put("language", "en");
                    metadata.put("chunk_index", i);
                    metadata.put("paragraph_count", paragraphs.length);
                    if (tags != null && tags.length > 0) {
                        metadata.put("tags", String.join(", ", tags));
                    }

                    documents.add(new Document(paragraph, metadata));
                }

                log.info("Loaded: {} ({} chunks)", filename, paragraphs.length);
            }

        } catch (Exception e) {
            log.error("Error uploading documents from {}: {}", locationPattern, e.getMessage(), e);
            throw new RuntimeException("Failed to upload documents", e);
        }

        log.info("Total documents prepared for ingestion: {}", documents.size());
        return documents;
    }

    /**
     * Uploads with explicit DocumentMetadata object.
     *
     * @param locationPattern resource pattern
     * @param metadata        structured metadata
     * @return list of Document objects
     */
    public List<Document> uploadDocuments(String locationPattern, DocumentMetadata metadata) {
        return uploadDocuments(locationPattern, metadata.category(), metadata.author(), metadata.tags());
    }
}
