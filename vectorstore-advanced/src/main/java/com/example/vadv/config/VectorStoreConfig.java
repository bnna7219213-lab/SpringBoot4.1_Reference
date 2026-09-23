package com.example.vadv.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Advanced VectorStore configuration with observability integration.
 *
 * Provides:
 * - SimpleVectorStore (in-memory) for demo mode
 * - Observation convention for Micrometer tracing
 * - BatchingStrategy configuration via properties
 *
 * Switch to PGVector by activating the 'pgvector' profile.
 */
@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    /**
     * Creates the VectorStore bean using SimpleVectorStore with the configured EmbeddingModel.
     *
     * In production (pgvector profile), Spring AI auto-configures PGVectorVectorStore
     * when spring-ai-starter-vector-store-pgvector is on the classpath.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("Initializing SimpleVectorStore with embedding model: {}",
                embeddingModel.getClass().getSimpleName());

        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        log.info("SimpleVectorStore initialized successfully (in-memory mode)");
        return vectorStore;
    }

    /**
     * Custom observation convention for VectorStore operations.
     * Integrates with Micrometer for tracing and metrics.
     */
    @Bean
    public VectorStoreObservationConvention vectorStoreObservationConvention() {
        return new DefaultVectorStoreObservationConvention();
    }
}
