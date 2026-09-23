# spring-ai-advanced

Spring Boot 4.1 + Spring AI 2.0 advanced demonstration project.

Demonstrates function calling, structured output, custom advisors,
multi-model routing, message history, and prompt templates.

## Prerequisites

- JDK 21
- Maven 3.9+
- (Optional) OpenAI API key

## Quick Start

```bash
# Set your API key (optional — runs in demo mode without)
export OPENAI_API_KEY=sk-your-key-here

# Run
mvn spring-boot:run
```

The application starts on **port 8081**.

## Endpoints

### 1. Function Calling / Tool Use
```bash
curl -X POST http://localhost:8081/api/advanced/tools \
  -H 'Content-Type: application/json' \
  -H 'X-User-Id: user-alice' \
  -d '{"message":"What is the weather in Berlin, Germany? Also get the 3-day forecast."}'
```

### 2. Structured Output — Text Classification
```bash
curl -X POST http://localhost:8081/api/advanced/structured/classify \
  -H 'Content-Type: application/json' \
  -d '{"text":"This product changed my life! Highly recommended to everyone."}'
```

### 3. Structured Output — Weather Report (Typed)
```bash
curl -X POST http://localhost:8081/api/advanced/structured/weather \
  -H 'Content-Type: application/json' \
  -d '{"city":"Paris"}'
```

### 4. Custom Advisors (Logging + Rate Limiting)
```bash
curl -X POST http://localhost:8081/api/advanced/advisors \
  -H 'Content-Type: application/json' \
  -H 'X-User-Id: user-bob' \
  -d '{"message":"Explain quantum entanglement in 2 sentences"}'
```

### 5. Multi-Model Routing
```bash
# Route to fast model
curl -X POST http://localhost:8081/api/advanced/route \
  -H 'Content-Type: application/json' \
  -d '{"message":"What is 15 * 23?","complexity":"fast"}'

# Route to complex model for reasoning
curl -X POST http://localhost:8081/api/advanced/route \
  -H 'Content-Type: application/json' \
  -d '{"message":"Explain the philosophical implications of Gödel incompleteness theorems","complexity":"complex"}'
```

### 6. Chat with Memory (Multi-turn)
```bash
# First turn — set context
curl -X POST http://localhost:8081/api/advanced/memory \
  -H 'Content-Type: application/json' \
  -d '{"message":"My name is Alice and I am learning Spring Framework","userId":"alice","reset":true}'

# Second turn — model remembers
curl -X POST http://localhost:8081/api/advanced/memory \
  -H 'Content-Type: application/json' \
  -d '{"message":"What did you learn about me?","userId":"alice"}'
```

### 7. Prompt Template
```bash
curl -X POST http://localhost:8081/api/advanced/template \
  -H 'Content-Type: application/json' \
  -d '{
    "systemPrompt": "You are a senior Java developer reviewing code.",
    "context": "The codebase uses Spring Boot 4.1",
    "style": "detailed with suggestions",
    "userMessage": "Review: public class UserService { private List users = new ArrayList(); }"
  }'
```

### 8. Informational Endpoints
```bash
# List available tools
curl http://localhost:8081/api/advanced/tools

# Model info
curl http://localhost:8081/api/advanced/model-info

# Rate limit status
curl -H 'X-User-Id: user-bob' http://localhost:8081/api/advanced/rate-limit

# Health
curl http://localhost:8081/actuator/health
```

## Project Structure

```
spring-ai-advanced/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/aiadv/
    │   ├── SpringAiAdvancedApplication.java
    │   ├── config/
    │   │   └── AdvancedAiConfig.java         # Multiple ChatClients + models + memory
    │   ├── service/
    │   │   ├── AdvancedChatService.java      # All 5 advanced features
    │   │   └── WeatherTool.java              # Function calling tool
    │   ├── advisor/
    │   │   └── CustomLoggingAdvisor.java     # Logging + rate limiting
    │   ├── model/
    │   │   └── StructuredOutput.java         # Record types for structured data
    │   └── controller/
    │       └── AdvancedChatController.java   # 11 REST endpoints
    └── resources/
        └── application.yml                    # All AI configuration
```

## Key Concepts

### Function Calling
The `WeatherTool` is a `@Component` with `@Tool` annotated methods.
When the user asks about weather, the LLM decides to invoke tool calls.
Spring AI handles: tool invocation, result merging, follow-up response generation.

### Structured Output
The `structuredChatClient` uses low temperature (0.0) for deterministic JSON.
The response is deserialized into Java records via `ChatClient.entity()`:
```java
Classification result = structuredChatClient.prompt()
    .user(text)
    .call()
    .entity(Classification.class);
```

### Custom Advisors
`CustomLoggingAdvisor` implements `BaseAdvisor` and plugs into the request chain:
- Logs request/response
- Enforces per-user rate limits (30 req/min sliding window)
- Validates message length
- Provides rate limit stats via static method

### Multi-Model Routing
Four ChatClient variants:
- **default** — balanced, with memory + advisors
- **fast** — cheap model, low temp, stateless
- **complex** — capable model with chain-of-thought
- **structured** — optimized for JSON output

### Message History
`MessageWindowChatMemory` + `MessageChatMemoryAdvisor` automatically
includes past turns as context via the `chat_memory_conversation_id`
advisor parameter.

## Provider Compatibility

Same as spring-ai-basic. Swap `spring.ai.openai.api-key` and `spring.ai.openai.base-url`
to use any OpenAI-compatible provider.
