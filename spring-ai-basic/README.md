# spring-ai-basic

Spring Boot 4.1 + Spring AI 2.0 demonstration project.

Connects to an OpenAI-compatible API to send prompts, receive responses,
and stream token-by-token via Server-Sent Events.

## Prerequisites

- JDK 21
- Maven 3.9+
- (Optional) OpenAI API key or any OpenAI-compatible provider

## Quick Start

### 1. Set your API key (optional — runs in stub mode without one)

```bash
# Option A: Environment variable
set OPENAI_API_KEY=sk-your-key-here    # Windows
export OPENAI_API_KEY=sk-your-key-here # Linux/Mac

# Option B: Edit application.yml
# spring.ai.openai.api-key: sk-your-key-here
```

### 2. Run

```bash
mvn spring-boot:run
```

### 3. Try the endpoints

```bash
# Synchronous chat
curl -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"What is Spring AI?"}'

# Streaming chat (SSE)
curl -N http://localhost:8080/api/chat/stream?message=Tell%20me%20a%20joke

# Chat with prompt template
curl -X POST http://localhost:8080/api/chat/template \
  -H 'Content-Type: application/json' \
  -d '{"templateName":"system-message.st","userMessage":"Explain recursion in programming","language":"en"}'

# Model info
curl http://localhost:8080/api/chat/model-info

# Health check
curl http://localhost:8080/actuator/health
```

## Project Structure

```
spring-ai-basic/
├── pom.xml                          # Maven build, Spring Boot 4.1 parent
├── README.md
└── src/main/
    ├── java/com/example/aibasic/
    │   ├── SpringAiBasicApplication.java    # Main entry point
    │   ├── config/
    │   │   └── AiConfig.java                # ChatClient & model configuration
    │   ├── service/
    │   │   └── ChatService.java             # ChatService (sync, stream, template)
    │   └── controller/
    │       └── ChatController.java          # REST endpoints
    └── resources/
        ├── application.yml                  # Spring AI config, retry, timeouts
        └── prompts/
            └── system-message.st            # System prompt template
```

## Key Concepts Demonstrated

### ChatClient

The central API for all AI interactions. Created from a `ChatModel` with a system prompt:

```java
return ChatClient.builder(chatModel)
    .defaultSystem(s -> s.text(systemTemplate)...)
    .build();
```

### Prompt Templates

Externalized prompts in `resources/prompts/` with `{{variable}}` syntax,
resolved at runtime via `PromptTemplate`.

### Retry Configuration

Exponential backoff retry on transient failures (5xx, timeouts):

```yaml
spring.ai.openai.chat.retry:
  max-attempts: 3
  backoff:
    initial-interval: 1000
    multiplier: 2.0
    max-interval: 10000
```

### Stub Mode

When no real API key is configured (default: `demo`), all endpoints return
informative stub messages instead of making API calls. This makes the
project runnable immediately for exploration.

### Streaming

Uses Project Reactor `Flux` for non-blocking streaming, exposed via
`SseEmitter` to support Server-Sent Events in the browser.

## Provider Compatibility

Works with any OpenAI-compatible API:

| Provider    | Base URL                             | Model              |
|-------------|--------------------------------------|--------------------|
| OpenAI      | https://api.openai.com/              | gpt-4o-mini        |
| DeepSeek    | https://api.deepseek.com             | deepseek-chat      |
| Local Ollama| http://localhost:11434               | llama3.2           |
| Custom LLM  | Your endpoint                        | Your model         |

Override `spring.ai.openai.base-url` and `spring.ai.openai.chat.options.model`
to switch providers without changing code.
