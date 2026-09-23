package com.example.vstore;

import com.example.vstore.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class VectorstoreBasicApplication {

    private static final Logger log = LoggerFactory.getLogger(VectorstoreBasicApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VectorstoreBasicApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(VectorStoreService vectorStoreService) {
        return args -> {
            log.info("========================================");
            log.info("  VectorStore Basic Demo Starting...");
            log.info("========================================");

            // Step 1: Ingest sample documents from classpath
            log.info("Step 1: Ingesting sample documents...");
            int ingested = vectorStoreService.ingestDocuments("classpath:documents/*.txt");
            log.info("Ingested {} document(s)", ingested);

            // Step 2: Demonstrate similarity search with various queries
            log.info("\nStep 2: Running similarity searches...\n");

            List<Map<String, Object>> results;

            // Query 1: Basic search
            log.info("--- Query 1: 'What is machine learning?' ---");
            results = vectorStoreService.search("What is machine learning?", 3);
            results.forEach(r -> log.info("Score: {} | Content: {}",
                String.format("%.4f", r.get("score")),
                r.get("content")));

            // Query 2: Semantic search
            log.info("\n--- Query 2: 'neural network deep learning' ---");
            results = vectorStoreService.search("neural network deep learning", 2);
            results.forEach(r -> log.info("Score: {} | Content: {}",
                String.format("%.4f", r.get("score")),
                r.get("content")));

            // Query 3: Domain-specific search
            log.info("\n--- Query 3: 'NLP language models transformers' ---");
            results = vectorStoreService.search("NLP language models transformers", 3);
            results.forEach(r -> log.info("Score: {} | Content: {}",
                String.format("%.4f", r.get("score")),
                r.get("content")));

            // Query 4: Vector search use case
            log.info("\n--- Query 4: 'vector embeddings semantic search' ---");
            results = vectorStoreService.search("vector embeddings semantic search", 2);
            results.forEach(r -> log.info("Score: {} | Content: {}",
                String.format("%.4f", r.get("score")),
                r.get("content")));

            log.info("\n========================================");
            log.info("  Demo completed successfully!");
            log.info("========================================");
        };
    }
}
