# vs-ai-basic

Basic RAG (Retrieval Augmented Generation) demonstration using Spring Boot 4.1 + Spring AI.

## Architecture

```
User Question
      |
      v
[Similarity Search] --> VectorStore (in-memory, by document chunks)
      |
      v
[Prompt Augmentation] --> Context injected into prompt
      |
      v
[LLM Call] --> OpenAI GPT-4o-mini via ChatClient
      |
      v
Answer (grounded in retrieved knowledge)
```

## Key Components

- **SimpleVectorStore**: In-memory vector store using embeddings
- **QuestionAnswerAdvisor**: Auto-performs similarity search and injects context
- **TokenTextSplitter**: Splits documents into 800-token chunks with overlap
- **ChatClient**: Spring AI fluent API for LLM interaction with advisor chain

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/rag/ask` | Ask a question, get RAG-augmented answer |
| POST | `/api/rag/ingest` | Re-ingest knowledge base documents |
| GET | `/api/rag/stats` | View vector store statistics |

## Running

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=sk-your-key-here

# Run the application
./mvnw spring-boot:run
```

## Example Usage

```bash
# Ask a question
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the return policy for electronics?"}'

# Get stats
curl http://localhost:8080/api/rag/stats

# Re-ingest with custom directory
curl -X POST http://localhost:8080/api/rag/ingest?dir=documents
```

## Knowledge Base

The `src/main/resources/documents/` directory contains 4 knowledge base files:

- **product-info.txt**: Product catalog (Cloud Platform, Data Pipeline, AI Studio, Security Shield)
- **policies.txt**: Return/refund policies, privacy policy, SLA, billing terms
- **faq.txt**: Frequently asked questions with answers
- **company-overview.txt**: Company information, leadership, statistics
