package com.example.vsadv;

import com.example.vsadv.service.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class VsAiAdvancedApplication {

    private static final Logger log = LoggerFactory.getLogger(VsAiAdvancedApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VsAiAdvancedApplication.class, args);
    }

    @Bean
    CommandLineRunner onStartup(DocumentIngestionService ingestionService) {
        return args -> {
            log.info("=== Starting advanced multi-source document ingestion ===");
            int ingested = ingestionService.ingestAllSources();
            log.info("=== Advanced ingestion complete: {} documents processed ===", ingested);
        };
    }
}
