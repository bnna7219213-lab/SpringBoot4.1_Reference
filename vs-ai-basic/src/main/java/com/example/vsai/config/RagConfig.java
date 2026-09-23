package com.example.vsai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Configuration
public class RagConfig {

    /**
     * Creates the VectorStore backed by the default EmbeddingModel.
     * SimpleVectorStore is an in-memory implementation suitable for demos
     * and small knowledge bases (< 10K documents).
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    /**
     * Creates the ChatClient with a system prompt that instructs the LLM
     * to use only the provided context for answering (RAG pattern).
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a knowledgeable assistant for the Acme Corporation.
                        You must answer questions using ONLY the information provided
                        in the context below. If the context does not contain enough
                        information to answer the question, respond with:
                        "I don't have enough information to answer that question."
                        Always be concise, professional, and accurate.
                        """)
                .build();
    }

    /**
     * Creates the QuestionAnswerAdvisor that automatically:
     * 1. Takes the user's question
     * 2. Performs similarity search in the VectorStore
     * 3. Injects the top-k relevant document chunks into the prompt context
     * 4. Sends the augmented prompt to the LLM
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return new QuestionAnswerAdvisor(vectorStore);
    }

    /**
     * TokenTextSplitter splits documents into chunks of ~800 tokens
     * with overlap to preserve context across chunk boundaries.
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(800, 100, 5, 10000, true);
    }

    /**
     * Bean that resolves classpath resources from the documents directory.
     */
    @Bean
    public ResourcePatternResolver resourcePatternResolver() {
        return new PathMatchingResourcePatternResolver();
    }
}
