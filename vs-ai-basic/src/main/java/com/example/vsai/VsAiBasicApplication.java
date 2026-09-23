package com.example.vsai;

import com.example.vsai.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VsAiBasicApplication {

    private static final Logger log = LoggerFactory.getLogger(VsAiBasicApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VsAiBasicApplication.class, args);
    }

    @Bean
    CommandLineRunner onStartup(RagService ragService) {
        return args -> {
            log.info("=== Starting knowledge base ingestion ===");
            int ingested = ragService.ingestKnowledge("documents");
            log.info("=== Ingested {} documents from knowledge base ===", ingested);

            // Run a demo query
            String testQuestion = "What is the return policy for electronics?";
            log.info("=== Running test query: '{}' ===", testQuestion);
            String answer = ragService.askQuestion(testQuestion);
            log.info("=== Test answer: {} ===", answer);
        };
    }
}
