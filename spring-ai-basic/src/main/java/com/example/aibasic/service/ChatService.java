package com.example.aibasic.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service layer for Spring AI chat operations.
 *
 * <p>Demonstrates four usage patterns:</p>
 * <ol>
 *   <li><strong>Simple chat</strong> — synchronous prompt to full response</li>
 *   <li><strong>Streaming chat</strong> — Server-Sent Events (SSE) for token-by-token output</li>
 *   <li><strong>Template-based chat</strong> — prompt substitution with named parameters</li>
 *   <li><strong>Graceful degradation</strong> — returns a helpful stub message when no real API is configured</li>
 * </ol>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    /**
     * The "demo" key is the placeholder from application.yml. When it is set,
     * we are in stub mode and should not attempt to call a real API.
     */
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * Whether to add the SimpleLoggerAdvisor to log request/response pairs.
     */
    @Value("${spring.ai.basic.enable-logging-advisor:true}")
    private boolean enableLoggingAdvisor;

    /**
     * System prompt template for the basic chat (used when no custom template is provided).
     */
    private static final String DEFAULT_SYSTEM = """
            You are a knowledgeable and friendly AI assistant.
            Answer questions concisely and accurately.
            If asked about topics beyond your knowledge, be honest about it.
            """;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Send a message and receive the full response synchronously.
     *
     * @param message the user's message
     * @return the assistant's complete response
     */
    public String chat(String message) {
        if (isStubMode()) {
            return stubResponse("chat", message);
        }

        log.debug("chat() called with message length={}", message.length());

        ChatClient.ChatClientRequestSpec request = chatClient.prompt()
                .user(message);

        if (enableLoggingAdvisor) {
            request.advisors(new SimpleLoggerAdvisor());
        }

        return request.call().content();
    }

    /**
     * Send a message and stream the response token-by-token.
     * Returns a Flux of response chunks suitable for SSE.
     *
     * @param message the user's message
     * @return a Flux of response tokens
     */
    public Flux<String> chatStream(String message) {
        if (isStubMode()) {
            // When in stub mode, emit a single "token" with the full stub response
            return Flux.just(stubResponse("stream", message));
        }

        log.debug("chatStream() called with message length={}", message.length());

        ChatClient.ChatClientRequestSpec request = chatClient.prompt()
                .user(message);

        if (enableLoggingAdvisor) {
            request.advisors(new SimpleLoggerAdvisor());
        }

        return request.stream().content();
    }

    /**
     * Chat using a named prompt template with substitution parameters.
     * The template is loaded from the classpath resources/prompts/ directory.
     *
     * @param params map containing keys: {@code templateName} (required) — the template file name,
     *               plus any other keys used as template variables
     * @return the assistant's response
     */
    public String chatWithTemplate(Map<String, Object> params) {
        if (isStubMode()) {
            return stubResponse("template", params.toString());
        }

        String templateName = (String) params.getOrDefault("templateName", "system-message.st");
        log.debug("chatWithTemplate() called with templateName={}", templateName);

        Resource templateResource = new org.springframework.core.io.DefaultResourceLoader()
                .getResource("classpath:prompts/" + templateName);

        PromptTemplate promptTemplate = new PromptTemplate(templateResource);

        // Add the user message as a parameter if provided
        @SuppressWarnings("unchecked")
        Map<String, Object> templateParams = new java.util.HashMap<>(params);
        templateParams.computeIfAbsent("userMessage", k -> "Hello, please introduce yourself!");

        var prompt = promptTemplate.create(templateParams);

        ChatClient.ChatClientRequestSpec request = chatClient.prompt(prompt);

        if (enableLoggingAdvisor) {
            request.advisors(new SimpleLoggerAdvisor());
        }

        return request.call().content();
    }

    /**
     * Check whether the application is running in stub/demo mode.
     */
    public boolean isStubMode() {
        return apiKey == null || apiKey.isBlank() || "demo".equals(apiKey);
    }

    /**
     * Return model configuration info.
     */
    public Map<String, Object> getModelInfo() {
        return Map.of(
                "provider", "openai-compatible",
                "model", "gpt-4o-mini",
                "stubMode", isStubMode(),
                "streamingAvailable", true,
                "templateSupported", true,
                "timestamp", java.time.Instant.now().toString()
        );
    }

    private String stubResponse(String mode, String input) {
        return String.format("""
                ╔══════════════════════════════════════════════════════╗
                ║  STUB MODE — No real API call was made               ║
                ╠══════════════════════════════════════════════════════╣
                ║  You are running with OPENAI_API_KEY=demo            ║
                ║  Set real key to connect to an AI provider           ║
                ╠══════════════════════════════════════════════════════╣
                ║  Mode: %-46s║
                ║  Input preview: %-37s║
                ║  Would call model: gpt-4o-mini                       ║
                ╚══════════════════════════════════════════════════════╝
                """,
                mode,
                input.substring(0, Math.min(input.length(), 35))
        ).stripTrailing();
    }
}
