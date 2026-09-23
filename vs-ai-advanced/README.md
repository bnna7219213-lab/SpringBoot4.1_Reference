# vs-ai-advanced

Production-grade RAG (Retrieval Augmented Generation) demonstration using Spring Boot 4.1 + Spring AI.

## Advanced Capabilities

`
User Question
      |
      v
[Query Rewriting] --> Expand acronyms, add domain terms, improve retrieval
      |
      v
[Multi-Modal Search] --> Vector search / Keyword search / Hybrid search
      |
      v
[Reranking] --> Multi-signal scoring (vector + keyword + source quality)
      |
      v
[Context Augmentation] --> Build ranked context from top-K reranked chunks
      |
      v
[LLM Generation] --> GPT-4o with strict grounded-only instructions
      |
      v
Answer + Metrics (timing, scores, source attribution)
`

## Key Components

- **DocumentIngestionService**: Multi-source ingestion with cleaning, metadata enrichment, batching
- **RerankingAdvisor**: Custom advisor using cosine similarity, keyword score, and source quality
- **QueryRewritingAdvisor**: Lightweight heuristic acronym expansion for better retrieval
- **AdvancedRagService**: Full RAG pipeline with caching, hybrid search, and observability
- **RagEvaluationService**: Computes precision@K, recall@K, MRR, and answer completeness
- **RetrievalResult**: Rich result model tracking all pipeline stages and timing

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v2/rag/ask | Full RAG pipeline (rewrite + retrieve + rerank + generate) |
| POST | /api/v2/rag/ask/simple | Simple RAG via Spring AI QuestionAnswerAdvisor |
| POST | /api/v2/rag/ingest | Re-ingest all knowledge base documents |
| GET | /api/v2/rag/stats | Ingestion statistics |
| POST | /api/v2/rag/evaluate | Run evaluation with custom questions |
| GET | /api/v2/rag/evaluate/demo | Run pre-defined evaluation with 5 test questions |

## Running

`ash
export OPENAI_API_KEY=sk-your-key-here
./mvnw spring-boot:run
`

The application starts on port 8081.

## Example Usage

`ash
# Full advanced RAG with hybrid search
curl -X POST http://localhost:8081/api/v2/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the RPO for Enterprise tier?", "mode": "hybrid"}'

# Simple RAG
curl -X POST http://localhost:8081/api/v2/rag/ask/simple \
  -H "Content-Type: application/json" \
  -d '{"question": "How do I upgrade my plan?"}'

# Run demo evaluation
curl http://localhost:8081/api/v2/rag/evaluate/demo

# Custom evaluation
curl -X POST http://localhost:8081/api/v2/rag/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "questions": ["What is the SLA for Enterprise?", "How do I get a refund?"],
    "expectedTerms": [["99.99", "uptime"], ["refund", "30 days"]]
  }'
`

## Configuration

All RAG pipeline parameters are configurable via pplication.yml:

`yaml
rag:
  ingestion:
    chunk-size: 600        # Tokens per chunk
    chunk-overlap: 80      # Overlap between chunks
    batch-size: 50         # Batch insert size for embedding calls
  search:
    top-k: 10              # Initial retrieval count
    rerank-top-k: 5        # Results after reranking
    similarity-threshold: 0.65
  hybrid:
    vector-weight: 0.7     # Weight for vector scores
    keyword-weight: 0.3    # Weight for keyword scores
  rewrites:
    enabled: true
    max-rewrites: 3
`

## Observability

The application exposes Micrometer metrics:
- ag.pipeline.total - Total pipeline duration
- ag.phase.retrieval - Retrieval phase duration (tagged with mode)
- ag.phase.reranking - Reranking duration
- ag.phase.generation - LLM generation duration
- ag.reranking.candidates - Number of candidates processed
- ag.query.success / ag.query.failure - Success/failure counters
- ag.eval.precision - Evaluation precision distribution

Metrics available at /actuator/prometheus.

## Knowledge Base

Four knowledge documents with rich metadata:
- **product-catalog.txt**: Full product specifications and pricing
- **corporate-policies.txt**: SLA, privacy, security, and cancellation policies
- **technical-specifications.txt**: Compute, storage, networking, API, and compliance specs
- **faq.txt**: 12 detailed FAQ entries with migration and compliance details
