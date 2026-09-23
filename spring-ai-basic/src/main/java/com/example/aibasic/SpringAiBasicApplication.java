package com.example.aibasic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Spring AI Basic — Spring Boot 4.1 + Spring AI 2.0 demonstration.
 *
 * <p>Core features:</p>
 * <ul>
 *   <li>Connecting to an OpenAI-compatible API via spring-ai-starter-model-openai</li>
 *   <li>Synchronous prompt/response with ChatClient</li>
 *   <li>Streaming responses with Server-Sent Events (SSE)</li>
 *   <li>Prompt templates loaded from classpath resources</li>
 *   <li>Graceful degradation when no API key is configured (stub mode)</li>
 * </ul>
 *
 * <p>Endpoints:</p>
 * <pre>
 *   POST   /api/chat                      — Send a message, receive full response
 *   GET    /api/chat/stream               — SSE streaming chat
 *   POST   /api/chat/template             — Chat with prompt-template parameters
 *   GET    /api/chat/model-info           — Current model configuration
 *   GET    /actuator/health               — Health check
 *   GET    /actuator/info                 — App info
 * </pre>
 */
@SpringBootApplication
public class SpringAiBasicApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringAiBasicApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringAiBasicApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String keyStatus = System.getenv("OPENAI_API_KEY") != null && !System.getenv("OPENAI_API_KEY").isBlank()
                ? "SET" : "NOT SET (demo/stub mode)";
        log.info(
            "============================================================={}" +
            "{}  Spring AI Basic running on port 8080!                    {}" +
            "{}  OPENAI_API_KEY: {}                                       {}" +
            "{}  Model: gpt-4o-mini                                      {}" +
            "{}  Try: POST /api/chat with {{\"message\":\"Hello!\"}}      {}" +
            "=============================================================",
            System.lineSeparator(), System.lineSeparator(), System.lineSeparator(),
            keyStatus, System.lineSeparator(), System.lineSeparator(), System.lineSeparator()
        );
    }
}
