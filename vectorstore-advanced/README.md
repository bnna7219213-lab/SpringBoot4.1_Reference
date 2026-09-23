# vectorstore-advanced

Advanced Spring AI VectorStore Demo with metadata filtering, REST API, and observability.

## Overview

This project demonstrates advanced features of Spring AI's VectorStore abstraction:

- **Metadata filtering** - Filter search results by category, author, date, tags
- **Similarity thresholds** - Control result quality with configurable thresholds
- **Document CRUD** - Create, read, update, delete operations
- **REST API** - Full HTTP interface for ingestion and search
- **Observability** - Micrometer Observation integration for tracing/metrics
- **Profile-based configuration** - Switch between in-memory and PGVector stores
- **BatchingStrategy** - Configurable document batching for ingestion

## Tech Stack

- Spring Boot 4.1.0
- JDK 21
- Spring AI 1.0.0
- Spring AI Transformers (local ONNX embeddings)
- Micrometer Observability (tracing + metrics)

## Running

`ash
# Default profile (in-memory SimpleVectorStore)
mvn spring-boot:run

# With PGVector profile (requires PostgreSQL with pgvector extension)
mvn spring-boot:run -Dspring-boot.run.profiles=pgvector
`

## REST API Endpoints

### Info
`
GET /api/v1/vectors/info
`
Returns service and vector store configuration.

### Search
`
GET /api/v1/vectors/search?query=embedding+models&topK=5
GET /api/v1/vectors/search?query=scaling&category=guide&topK=3
GET /api/v1/vectors/search?query=memory&category=research&author=Dr.+Smith
GET /api/v1/vectors/search?query=rag&threshold=0.6&topK=5
`

### Ingest
`
POST /api/v1/vectors/ingest
Content-Type: application/json

{
    "locationPattern": "classpath:documents/*.txt",
    "category": "overview",
    "author": "Demo User",
    "tags": ["intro", "demo"]
}
`

### Delete
`
DELETE /api/v1/vectors
Content-Type: application/json

["doc-id-1", "doc-id-2"]
`

## Document Structure

Documents are loaded from classpath resources with metadata:
- introduction.txt - category: overview
- esearch-notes.txt - category: research
- production-guide.txt - category: guide
- semantic-search.txt - category: research
- ag-systems.txt - category: research

## Profile Configuration

### inmemory (default)
Uses SimpleVectorStore with local ONNX embeddings. No external dependencies.

### pgvector
Uses PostgreSQL with the pgvector extension for persistent vector storage.
Configure spring.datasource.* properties for your database connection.

## Observability

Operations are annotated with @Observed for Micrometer Observation integration.
Traces and metrics are exposed via Spring Boot Actuator.

- GET /actuator/observations - View observation data
- GET /actuator/prometheus - Prometheus metrics endpoint
