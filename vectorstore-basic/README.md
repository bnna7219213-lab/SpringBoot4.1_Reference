# vectorstore-basic

Spring AI VectorStore Basic Demo with SimpleVectorStore and local ONNX embeddings.

## Overview

This project demonstrates Spring AI's unified `VectorStore` interface using an in-memory
`SimpleVectorStore` backed by a local ONNX embedding model (no API key required).

### Key Concepts

- **`VectorStore` abstraction** - Spring AI provides a unified interface for working with
  vector databases. Implementations available include SimpleVectorStore, PGVector,
  Milvus, Chroma, Weaviate, Pinecone, and more.
- **`SimpleVectorStore`** - An in-memory vector store perfect for demos and testing.
- **`EmbeddingModel`** - Uses `TransformersEmbeddingModel` from spring-ai-transformers to
  run a small ONNX model locally (`sentence-transformers/all-MiniLM-L6-v2`).
- **`SearchRequest`** - Configurable search with topK and similarity thresholds.

## Tech Stack

- Spring Boot 4.1.0
- JDK 21
- Spring AI 1.0.0
- Spring AI Transformers (local embeddings)

## Project Structure

```
vectorstore-basic/
+-- pom.xml
+-- README.md
+-- src/main/java/com/example/vstore/
|   +-- VectorstoreBasicApplication.java   # Main class with CommandLineRunner demo
|   +-- service/
|   |   +-- VectorStoreService.java         # Core VectorStore operations
|   +-- config/
|       +-- VectorStoreConfig.java          # VectorStore bean configuration
+-- src/main/resources/
    +-- application.yml
    +-- documents/
        +-- sample-data.txt                 # Sample AI/ML documents for ingestion
```

## Running

```bash
mvn spring-boot:run
```

On startup, the app will:
1. Ingest sample documents about AI/ML concepts
2. Run 4 similarity search queries
3. Print results with similarity scores

## How It Works

1. **Ingestion**: Documents are loaded from classpath resources, split into chunks, and
   embedded using the local ONNX model. Vectors are stored in the SimpleVectorStore.
2. **Search**: A query is embedded using the same model, then the most similar vectors
   are retrieved using cosine similarity.
3. **Results**: Each result includes the original text, similarity score, and metadata.

## Notes

- The local ONNX embedding model (~80MB) is downloaded on first run.
- No external API keys are needed for embeddings (OpenAI config is shown but not used for embeddings).
- For production, replace `SimpleVectorStore` with a persistent vector database like PGVector.
