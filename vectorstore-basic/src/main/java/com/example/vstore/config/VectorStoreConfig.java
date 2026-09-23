package com.example.vstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("Initializing SimpleVectorStore with embedding model: {}",
                embeddingModel.getClass().getSimpleName());

        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        log.info("SimpleVectorStore initialized successfully");
        return vectorStore;
    }
}
