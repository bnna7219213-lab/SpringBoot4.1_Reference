package com.example.vsai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final TokenTextSplitter textSplitter;
    private final ResourcePatternResolver resourceResolver;

    /** Tracks ingestion statistics */
    private final AtomicInteger totalDocumentsIngested = new AtomicInteger(0);
    private final AtomicInteger totalChunksStored = new AtomicInteger(0);
    private final Map<String, Integer> documentsBySource = new ConcurrentHashMap<>();

    public RagService(VectorStore vectorStore,
                      ChatClient chatClient,
                      QuestionAnswerAdvisor questionAnswerAdvisor,
                      TokenTextSplitter textSplitter,
                      ResourcePatternResolver resourceResolver) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.questionAnswerAdvisor = questionAnswerAdvisor;
        this.textSplitter = textSplitter;
        this.resourceResolver = resourceResolver;
    }

    public int ingestKnowledge(String directoryPath) {
        String locationPattern = "classpath:" + directoryPath + "/*.txt";
        Resource[] resources;
        try {
            resources = resourceResolver.getResources(locationPattern);
        } catch (IOException e) {
            log.error("Failed to resolve resources from: {}", locationPattern, e);
            throw new RuntimeException("Failed to load knowledge base documents", e);
        }

        if (resources.length == 0) {
            log.warn("No .txt documents found in {}", locationPattern);
            return 0;
        }

        AtomicInteger documentCount = new AtomicInteger(0);

        for (Resource resource : resources) {
            try {
                String source = resource.getFilename();
                log.info("Ingesting document: {}", source);

                // 1. Read the raw document
                TextReader reader = new TextReader(resource);
                reader.getCustomMetadata().put("source", source);
                reader.getCustomMetadata().put("ingestionId", UUID.randomUUID().toString());
                List<Document> rawDocuments = reader.get();

                // 2. Split into chunks
                List<Document> chunks = textSplitter.apply(rawDocuments);

                // 3. Tag each chunk with source metadata
                for (Document chunk : chunks) {
                    chunk.getMetadata().putIfAbsent("source", source);
                    chunk.getMetadata().putIfAbsent("ingestionTimestamp",
                            System.currentTimeMillis());
                }

                // 4. Store chunks in vector database
                vectorStore.add(chunks);

                // 5. Track stats
                documentCount.incrementAndGet();
                documentsBySource.merge(source, chunks.size(), Integer::sum);
                totalChunksStored.addAndGet(chunks.size());

                log.info("Document '{}' ingested: {} chunks created", source, chunks.size());

            } catch (Exception e) {
                log.error("Failed to ingest document: {}", resource.getFilename(), e);
            }
        }

        int count = documentCount.get();
        totalDocumentsIngested.addAndGet(count);
        log.info("Ingestion complete. Total documents: {}, Total chunks: {}",
                totalDocumentsIngested.get(), totalChunksStored.get());
        return count;
    }

    public String askQuestion(String question) {
        log.info("Received question: {}", question);

        // The QuestionAnswerAdvisor automatically:
        // 1. Converts question to embedding
        // 2. Performs similarity search in VectorStore
        // 3. Injects relevant context into the prompt
        String response = chatClient.prompt()
                .user(question)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        log.info("Question answered. Response length: {}", response != null ? response.length() : 0);
        return response;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalDocuments", totalDocumentsIngested.get());
        stats.put("totalChunks", totalChunksStored.get());
        stats.put("documentsBySource", Map.copyOf(documentsBySource));
        return stats;
    }
}
