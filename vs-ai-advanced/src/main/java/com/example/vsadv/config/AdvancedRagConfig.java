package com.example.vsadv.config;

import com.example.vsadv.advisor.QueryRewritingAdvisor;
import com.example.vsadv.advisor.RerankingAdvisor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class AdvancedRagConfig {

    private static final Logger log = LoggerFactory.getLogger(AdvancedRagConfig.class);

    @Value("${rag.ingestion.chunk-size:600}")
    private int chunkSize;

    @Value("${rag.ingestion.chunk-overlap:80}")
    private int chunkOverlap;

    @Value("${rag.search.top-k:10}")
    private int topK;

    @Value("${rag.search.rerank-top-k:5}")
    private int rerankTopK;

    @Value("${rag.search.similarity-threshold:0.65}")
    private double similarityThreshold;

    @Value("${rag.rewrites.enabled:true}")
    private boolean rewritesEnabled;

    @Value("${rag.rewrites.max-rewrites:3}")
    private int maxRewrites;

    @Value("${rag.hybrid.vector-weight:0.7}")
    private double vectorWeight;

    @Value("${rag.hybrid.keyword-weight:0.3}")
    private double keywordWeight;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are an expert AI assistant for ACME Corporation's advanced
                        knowledge system. You have access to retrieved context from multiple
                        authoritative sources including product documentation, policies,
                        technical specifications, and compliance reports.

                        INSTRUCTIONS:
                        1. Base your answer EXCLUSIVELY on the provided context.
                        2. Cite relevant sources when possible (include the Source field).
                        3. If multiple sources agree, synthesize them concisely.
                        4. If sources conflict, note the discrepancy and present both views.
                        5. If context is insufficient, say: "The knowledge base does not
                           contain sufficient information to fully answer this question."
                        6. Be precise, technical when appropriate, and never speculate.
                        7. Keep responses under 500 words unless the question demands detail.
                        """)
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true);
    }

    @Bean
    public ResourcePatternResolver resourcePatternResolver() {
        return new PathMatchingResourcePatternResolver();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("ragQueries", "rewriteQueries", "retrievalResults");
    }

    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return new QuestionAnswerAdvisor(vectorStore);
    }

    @Bean
    public RerankingAdvisor rerankingAdvisor(VectorStore vectorStore,
                                              EmbeddingModel embeddingModel,
                                              MeterRegistry meterRegistry) {
        return new RerankingAdvisor(vectorStore, embeddingModel, rerankTopK, similarityThreshold, meterRegistry);
    }

    @Bean
    public QueryRewritingAdvisor queryRewritingAdvisor(ChatClient chatClient,
                                                         MeterRegistry meterRegistry) {
        return new QueryRewritingAdvisor(chatClient, maxRewrites, rewritesEnabled, meterRegistry);
    }
}
