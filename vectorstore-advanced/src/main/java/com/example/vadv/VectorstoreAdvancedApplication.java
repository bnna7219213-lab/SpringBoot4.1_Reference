package com.example.vadv;

import com.example.vadv.service.AdvancedVectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class VectorstoreAdvancedApplication {

    private static final Logger log = LoggerFactory.getLogger(VectorstoreAdvancedApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VectorstoreAdvancedApplication.class, args);
    }

    @Bean
    public CommandLineRunner advancedDemo(AdvancedVectorStoreService service) {
        return args -> {
            log.info("========================================");
            log.info("  VectorStore Advanced Demo Starting");
            log.info("========================================");

            // 1. Ingest documents with metadata from different categories
            log.info("\n--- Phase 1: Ingestion with Metadata ---");

            int count1 = service.ingestWithMetadata(
                    "classpath:documents/introduction.txt",
                    "overview", "Dr. Smith", "introduction", "basics");
            log.info("Ingested introduction documents: {}", count1);

            int count2 = service.ingestWithMetadata(
                    "classpath:documents/research-notes.txt",
                    "research", "Prof. Zhang", "research", "embeddings", "indexing");
            log.info("Ingested research documents: {}", count2);

            int count3 = service.ingestWithMetadata(
                    "classpath:documents/production-guide.txt",
                    "guide", "Eng. Rivera", "production", "deployment", "scaling");
            log.info("Ingested production guide documents: {}", count3);

            int count4 = service.ingestWithMetadata(
                    "classpath:documents/semantic-search.txt",
                    "research", "Dr. Patel", "search", "semantic", "nlp");
            log.info("Ingested semantic search documents: {}", count4);

            int count5 = service.ingestWithMetadata(
                    "classpath:documents/rag-systems.txt",
                    "research", "Prof. Chen", "rag", "llm", "retrieval");
            log.info("Ingested RAG documents: {}", count5);

            log.info("Total ingested: {} documents", count1 + count2 + count3 + count4 + count5);

            // 2. Basic similarity search
            log.info("\n--- Phase 2: Basic Similarity Search ---");
            List<Map<String, Object>> results;

            log.info("Query: 'How to choose embedding models?'");
                        results = service.search("How to choose embedding models?", 3);
            results.forEach(r -> log.info("  Score: {} | {}",
                    String.format("%.4f", r.get("score")),
                    r.get("content")));

            // 3. Search with similarity threshold
            log.info("\n--- Phase 3: Search with Threshold ---");
            log.info("Query: 'vector database scaling strategies' (threshold=0.5)");
            results = service.searchWithThreshold("vector database scaling strategies", 5, 0.5);
            log.info("Results above threshold: {}", results.size());
            results.forEach(r -> log.info("  Score: {} | {}",
                    String.format("%.4f", r.get("score")),
                    r.get("content").toString()));
            // 4. Search with metadata filtering
            log.info("\n--- Phase 4: Filtered Search (category='research') ---");
            results = service.searchWithFilters("embedding models performance", 5, "research", null);
            log.info("Research results: {}", results.size());
            results.forEach(r -> log.info("  Score: {} | Meta: {} | {}",
                    String.format("%.4f", r.get("score")),
                    r.get("metadata"),
                    r.get("content")));
            // 5. Search with author filter
            log.info("\n--- Phase 5: Filtered Search (category='guide') ---");
            results = service.searchWithFilters("deployment memory management", 5, "guide", null);
            log.info("Guide results: {}", results.size());
            results.forEach(r -> log.info("  Score: {} | Category: {} | {}",
                    String.format("%.4f", r.get("score")),
                    ((Map) r.get("metadata")).get("category"),
                    r.get("content")));

            // 6. Show VectorStore info
            log.info("\n--- Phase 6: VectorStore Configuration ---");
            Map<String, String> info = service.getVectorStoreInfo();
            info.forEach((key, value) -> log.info("  {}: {}", key, value));

            log.info("\n========================================");
            log.info("  Advanced Demo completed!");
            log.info("  REST API available at:");
            log.info("  GET  http://localhost:8081/api/v1/vectors/info");
            log.info("  GET  http://localhost:8081/api/v1/vectors/search?query=your+query&topK=5");
            log.info("  GET  http://localhost:8081/api/v1/vectors/search?query=embedding&category=research");
            log.info("  POST http://localhost:8081/api/v1/vectors/ingest");
            log.info("  DELETE http://localhost:8081/api/v1/vectors");
            log.info("========================================");
        };
    }
}
