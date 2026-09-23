package com.example.aiadv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Spring AI Advanced — Spring Boot 4.1 + Spring AI 2.0 demonstration.
 *
 * <p>Core features:</p>
 * <ul>
 *   <li>Function calling / tool use — WeatherTool that the LLM can invoke</li>
 *   <li>Structured output — converting LLM responses into Java record types</li>
 *   <li>Custom advisors — logging, rate-limiting hooks in the request pipeline</li>
 *   <li>Multi-model routing — different models for different task complexities</li>
 *   <li>Message history — in-memory conversation memory across requests</li>
 *   <li>Prompt templates — parameterized prompts with data binding</li>
 * </ul>
 *
 * <p>Endpoints:</p>
 * <pre>
 *   POST   /api/advanced/chat               — Chat with tool-calling support
 *   POST   /api/advanced/structured         — Structured output (returns typed data)
 *   POST   /api/advanced/chat-with-memory   — Multi-turn chat with memory
 *   POST   /api/advanced/advisors           — Demo of custom advisor pipeline
 *   GET    /api/advanced/model-info         — Model configuration
 *   GET    /api/advanced/tools              — List available tool functions
 *   GET    /actuator/health                 — Health check
 * </pre>
 */
@SpringBootApplication
public class SpringAiAdvancedApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringAiAdvancedApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAdvancedApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String keyStatus = System.getenv("OPENAI_API_KEY") != null && !System.getenv("OPENAI_API_KEY").isBlank()
                ? "SET" : "NOT SET (demo/stub mode)";
        log.info(
            "======================================================================={}" +
            "{}  Spring AI Advanced running on port 8081!                          {}" +
            "{}  OPENAI_API_KEY: {}                                               {}" +
            "{}  Features: function-calling, structured-output, advisors, memory   {}" +
            "=======================================================================",
            System.lineSeparator(), System.lineSeparator(), System.lineSeparator(),
            keyStatus, System.lineSeparator(), System.lineSeparator()
        );
    }
}
