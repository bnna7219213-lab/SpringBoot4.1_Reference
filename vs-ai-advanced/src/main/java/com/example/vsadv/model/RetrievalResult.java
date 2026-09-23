package com.example.vsadv.model;

import java.util.List;
import java.util.Map;

/**
 * Represents the complete result of an advanced RAG query pipeline,
 * including retrieval metadata, reranking scores, and final answer.
 */
public class RetrievalResult {

    private String question;
    private String rewrittenQuestion;
    private String answer;
    private List<DocumentChunk> retrievedChunks;
    private List<DocumentChunk> rerankedChunks;
    private long retrievalTimeMs;
    private long rerankingTimeMs;
    private long generationTimeMs;
    private long totalTimeMs;
    private boolean fromCache;
    private String searchMode; // "vector", "keyword", "hybrid"

    public RetrievalResult() {}

    // -- Getters and Setters --

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getRewrittenQuestion() { return rewrittenQuestion; }
    public void setRewrittenQuestion(String rewrittenQuestion) { this.rewrittenQuestion = rewrittenQuestion; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<DocumentChunk> getRetrievedChunks() { return retrievedChunks; }
    public void setRetrievedChunks(List<DocumentChunk> retrievedChunks) { this.retrievedChunks = retrievedChunks; }

    public List<DocumentChunk> getRerankedChunks() { return rerankedChunks; }
    public void setRerankedChunks(List<DocumentChunk> rerankedChunks) { this.rerankedChunks = rerankedChunks; }

    public long getRetrievalTimeMs() { return retrievalTimeMs; }
    public void setRetrievalTimeMs(long retrievalTimeMs) { this.retrievalTimeMs = retrievalTimeMs; }

    public long getRerankingTimeMs() { return rerankingTimeMs; }
    public void setRerankingTimeMs(long rerankingTimeMs) { this.rerankingTimeMs = rerankingTimeMs; }

    public long getGenerationTimeMs() { return generationTimeMs; }
    public void setGenerationTimeMs(long generationTimeMs) { this.generationTimeMs = generationTimeMs; }

    public long getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }

    public boolean isFromCache() { return fromCache; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }

    public String getSearchMode() { return searchMode; }
    public void setSearchMode(String searchMode) { this.searchMode = searchMode; }

    /**
     * Represents a single document chunk with its metadata and scores.
     */
    public static class DocumentChunk {
        private String id;
        private String content;
        private String source;
        private double vectorScore;
        private double keywordScore;
        private double hybridScore;
        private double rerankScore;
        private int chunkIndex;
        private Map<String, Object> metadata;

        public DocumentChunk() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public double getVectorScore() { return vectorScore; }
        public void setVectorScore(double vectorScore) { this.vectorScore = vectorScore; }

        public double getKeywordScore() { return keywordScore; }
        public void setKeywordScore(double keywordScore) { this.keywordScore = keywordScore; }

        public double getHybridScore() { return hybridScore; }
        public void setHybridScore(double hybridScore) { this.hybridScore = hybridScore; }

        public double getRerankScore() { return rerankScore; }
        public void setRerankScore(double rerankScore) { this.rerankScore = rerankScore; }

        public int getChunkIndex() { return chunkIndex; }
        public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
