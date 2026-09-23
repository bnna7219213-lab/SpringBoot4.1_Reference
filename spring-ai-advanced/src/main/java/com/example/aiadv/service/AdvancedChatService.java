package com.example.aiadv.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.prompt.UserMessageTemplate;
import org.springframework.ai.template.validation.TemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.aiadv.advisor.CustomLoggingAdvisor;
import com.example.aiadv.model.StructuredOutput;

import reactor.core.publisher.Flux;

/**
 * Service layer demonstrating advanced Spring AI 2.0 features.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Function calling with WeatherTool</li>
 *   <li>Structured output conversion to Java records</li>
 *   <li>Custom advisor (logging + rate limiting)</li>
 *   <li>Multi-model routing (fast vs. complex)</li>
 *   <li>Message history with in-memory chat memory</li>
 *   <li>Prompt template with parameter binding</li>
 * </ul>
 */
@Service
public class AdvancedChatService {

    private static final Logger log = LoggerFactory.getLogger(AdvancedChatService.class);

    private final ChatClient defaultChatClient;
    private final ChatClient fastChatClient;
    private final ChatClient complexChatClient;
    private final ChatClient structuredChatClient;
    private final WeatherTool weatherTool;
    private final ChatMemory chatMemory;

    /**
     * In-memory store for multi-user conversation histories.
     * In production, use Redis or a database-backed ChatMemory implementation.
     */
    private final ConcurrentHashMap<String, List<InMemoryMessage>> conversationHistory
            = new ConcurrentHashMap<>();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.memory.enabled:true}")
    private boolean memoryEnabled;

    public AdvancedChatService(
            ChatClient defaultChatClient,
            @Qualifier("fastChatClient") ChatClient fastChatClient,
            @Qualifier("complexChatClient") ChatClient complexChatClient,
            @Qualifier("structuredChatClient") ChatClient structuredChatClient,
            WeatherTool weatherTool,
            ChatMemory chatMemory) {
        this.defaultChatClient = defaultChatClient;
        this.fastChatClient = fastChatClient;
        this.complexChatClient = complexChatClient;
        this.structuredChatClient = structuredChatClient;
        this.weatherTool = weatherTool;
        this.chatMemory = chatMemory;
    }

    // -----------------------------------------------------------------------
    // 1. Function Calling / Tool Use
    // -----------------------------------------------------------------------

    /**
     * Chat with function calling. The LLM can decide to call WeatherTool
     * methods when the user asks about weather.
     *
     * @param message    the user's message
     * @param userId     the user ID for rate limiting context
     * @return the assistant's response (potentially after tool calls)
     */
    public Map<String, Object> chatWithTools(String message, String userId) {
        if (isStubMode()) {
            return stubToolResponse(message);
        }

        log.info("chatWithTools() userId={} messageLength={}", userId, message.length());

        long start = System.currentTimeMillis();

        // Wire the tool + advisor into the request
        String response = defaultChatClient.prompt()
                .user(message)
                .tools(weatherTool)
                .advisors(new CustomLoggingAdvisor(userId))
                .call()
                .content();

        long duration = System.currentTimeMillis() - start;

        return Map.of(
                "response", response,
                "toolsAvailable", List.of("getCurrentWeather", "getForecast", "compareCities"),
                "userId", userId,
                "processingTimeMs", duration,
                "timestamp", Instant.now().toString()
        );
    }

    // -----------------------------------------------------------------------
    // 2. Structured Output
    // -----------------------------------------------------------------------

    /**
     * Extract structured data from unstructured text using the LLM.
     * The LLM response is parsed into a typed Java record.
     *
     * @param text the input text to analyze
     * @return structured classification result
     */
    public StructuredOutput.Classification classifyText(String text) {
        if (isStubMode()) {
            return stubClassification(text);
        }

        log.info("classifyText() textLength={}", text.length());

        int maxLen = Math.min(text.length(), 2000);

        return structuredChatClient.prompt()
                .system("""
                    Classify the following text. Return a JSON object with:
                    - category: one of [news, opinion, review, question, complaint, spam]
                    - sentiment: POSITIVE, NEGATIVE, NEUTRAL, or MIXED
                    - confidence: a float from 0.0 to 1.0
                    - keywords: a list of 2-5 relevant keywords
                    - requiresReview: true if the content might need human moderation
                    """)
                .user(text.substring(0, maxLen))
                .call()
                .entity(StructuredOutput.Classification.class);
    }

    /**
     * Generate a weather report structured as a typed record.
     *
     * @param city the city to get a report for
     * @return structured weather report
     */
    public StructuredOutput.WeatherReport getStructuredWeatherReport(String city) {
        if (isStubMode()) {
            return stubWeatherReport(city);
        }

        return structuredChatClient.prompt()
                .system("""
                    You are a weather data assistant. Return a JSON object matching this structure:
                    {
                      "city": string,
                      "country": string,
                      "temperature": number,
                      "unit": "C" or "F",
                      "conditions": string,
                      "humidity": integer (0-100),
                      "windSpeed": number,
                      "windUnit": "km/h" or "mph",
                      "forecast": [ { "date": string, "highTemp": number, "lowTemp": number,
                                     "conditions": string, "chanceOfRain": integer } ],
                      "summary": string
                    }
                    Use the getCurrentWeather tool to get real data, then format it as the report.
                    """)
                .user("Generate a detailed weather report for " + city + " with a 3-day forecast.")
                .tools(weatherTool)
                .call()
                .entity(StructuredOutput.WeatherReport.class);
    }

    // -----------------------------------------------------------------------
    // 3. Custom Advisors
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the custom advisor pipeline with logging and rate limiting.
     * The same request goes through the advisor chain before reaching the LLM.
     *
     * @param message the user's message
     * @param userId the user for rate-limiting context
     * @return the response with advisor metadata
     */
    public Map<String, Object> chatWithAdvisors(String message, String userId) {
        if (isStubMode()) {
            return stubAdvisorResponse(message, userId);
        }

        log.info("chatWithAdvisors() userId={}", userId);

        long start = System.currentTimeMillis();

        String response = defaultChatClient.prompt()
                .user(message)
                .advisors(
                        // CustomLoggingAdvisor first (highest precedence)
                        new CustomLoggingAdvisor(userId, true, true, 100),
                        // Then the standard logger
                        new org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor()
                )
                .call()
                .content();

        long duration = System.currentTimeMillis() - start;
        var rateLimitStats = CustomLoggingAdvisor.getRateLimitStats(userId);

        return Map.of(
                "response", response,
                "userId", userId,
                "advisorsApplied", List.of("CustomLoggingAdvisor", "SimpleLoggerAdvisor"),
                "processingTimeMs", duration,
                "rateLimitRemaining", rateLimitStats.get("remaining"),
                "timestamp", Instant.now().toString()
        );
    }

    // -----------------------------------------------------------------------
    // 4. Multi-Model Routing
    // -----------------------------------------------------------------------

    /**
     * Routes the chat request to the appropriate model based on complexity.
     *
     * @param message  the user's message
     * @param complexity "fast" for cheap/simple, "complex" for deep reasoning
     * @return the response with routing info
     */
    public Map<String, Object> chatWithRouting(String message, String complexity) {
        if (isStubMode()) {
            return stubRoutingResponse(message, complexity);
        }

        ChatClient client = switch (complexity.toLowerCase()) {
            case "fast" -> {
                log.info("Routing to fast model for: {}", message.substring(0, Math.min(50, message.length())));
                yield fastChatClient;
            }
            case "complex" -> {
                log.info("Routing to complex model for: {}", message.substring(0, Math.min(50, message.length())));
                yield complexChatClient;
            }
            default -> {
                log.info("Routing to default model for: {}", message.substring(0, Math.min(50, message.length())));
                yield defaultChatClient;
            }
        };

        long start = System.currentTimeMillis();

        String response = client.prompt()
                .user(message)
                .call()
                .content();

        long duration = System.currentTimeMillis() - start;

        return Map.of(
                "response", response,
                "routedTo", complexity.equalsIgnoreCase("fast") ? "fastChatClient" :
                           complexity.equalsIgnoreCase("complex") ? "complexChatClient" : "defaultChatClient",
                "model", complexity.equalsIgnoreCase("fast") ? "fastModel" :
                        complexity.equalsIgnoreCase("complex") ? "complexModel" : "defaultModel",
                "processingTimeMs", duration,
                "timestamp", Instant.now().toString()
        );
    }

    // -----------------------------------------------------------------------
    // 5. Chat with Memory
    // -----------------------------------------------------------------------

    /**
     * Multi-turn chat with in-memory conversation history.
     * The ChatClient automatically uses ChatMemory to include past messages.
     *
     * @param message the user's message
     * @param userId  the conversation/user ID
     * @param reset   if true, clears the history for this user first
     * @return the response with conversation context info
     */
    public Map<String, Object> chatWithMemory(String message, String userId, boolean reset) {
        if (isStubMode()) {
            return stubMemoryResponse(message, userId, reset);
        }

        if (reset) {
            conversationHistory.remove(userId);
            log.info("Memory reset for user {}", userId);
        }

        // Add user message to history
        conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(new InMemoryMessage("user", message));

        log.info("chatWithMemory() userId={} historySize={}", userId,
                conversationHistory.getOrDefault(userId, List.of()).size());

        long start = System.currentTimeMillis();

        // The defaultChatClient has MessageChatMemoryAdvisor wired
        String response = defaultChatClient.prompt()
                .user(message)
                .advisors(ctx -> ctx.param(ChatMemory_CONVERSATION_ID_KEY, userId))
                .call()
                .content();

        // Add assistant response to history
        conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(new InMemoryMessage("assistant", response));

        long duration = System.currentTimeMillis() - start;
        List<InMemoryMessage> history = conversationHistory.getOrDefault(userId, List.of());

        return Map.of(
                "response", response,
                "userId", userId,
                "historySize", history.size(),
                "conversationHistory", history,
                "memoryEnabled", memoryEnabled,
                "processingTimeMs", duration,
                "timestamp", Instant.now().toString()
        );
    }

    private static final String ChatMemory_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    /**
     * Simple record for in-memory message storage.
     */
    private record InMemoryMessage(String role, String content, Instant timestamp) {
        InMemoryMessage(String role, String content) {
            this(role, content, Instant.now());
        }
    }

    // -----------------------------------------------------------------------
    // Prompt Templates
    // -----------------------------------------------------------------------

    /**
     * Chat using a parameterized prompt template.
     * Shows how SystemPromptTemplate and handlebars-style binding work.
     *
     * @param params template variables: systemPrompt, userMessage, context, style
     * @return the response
     */
    public Map<String, Object> chatWithPromptTemplate(Map<String, Object> params) {
        if (isStubMode()) {
            return stubTemplateResponse(params);
        }

        String systemPrompt = (String) params.getOrDefault("systemPrompt",
                "You are a knowledgeable assistant.");
        String context = (String) params.getOrDefault("context", "");
        String style = (String) params.getOrDefault("style", "concise");

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate("""
                {systemPrompt}
                
                Additional context: {context}
                
                Response style: {style}
                """);

        var systemMessage = systemTemplate.createMessage(Map.of(
                "systemPrompt", systemPrompt,
                "context", context,
                "style", style
        ));

        String userMessage = (String) params.getOrDefault("userMessage", "Hello!");

        long start = System.currentTimeMillis();

        String response = defaultChatClient.prompt()
                .system(systemMessage.getText())
                .user(userMessage)
                .call()
                .content();

        long duration = System.currentTimeMillis() - start;

        return Map.of(
                "response", response,
                "templateParams", params,
                "processingTimeMs", duration,
                "timestamp", Instant.now().toString()
        );
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    public List<Map<String, Object>> getAvailableTools() {
        return List.of(
                Map.of(
                        "name", "getCurrentWeather",
                        "description", "Get current weather for a city",
                        "parameters", List.of("city", "country?", "unit?")
                ),
                Map.of(
                        "name", "getForecast",
                        "description", "Get multi-day forecast for a city",
                        "parameters", List.of("city", "days", "unit?")
                ),
                Map.of(
                        "name", "compareCities",
                        "description", "Compare weather between two cities",
                        "parameters", List.of("city1", "city2")
                )
        );
    }

    public Map<String, Object> getModelInfo() {
        return Map.of(
                "defaultModel", Map.of(
                        "modelId", "defaultChatClient",
                        "provider", "openai-compatible",
                        "temperature", 0.3,
                        "maxTokens", 4096,
                        "features", List.of("tools", "memory", "advisors")
                ),
                "fastModel", Map.of(
                        "modelId", "fastChatClient",
                        "provider", "openai-compatible",
                        "temperature", 0.1,
                        "maxTokens", 1024,
                        "features", List.of("fast", "stateless")
                ),
                "complexModel", Map.of(
                        "modelId", "complexChatClient",
                        "provider", "openai-compatible",
                        "temperature", 0.5,
                        "maxTokens", 8192,
                        "features", List.of("reasoning", "chain-of-thought")
                ),
                "structuredModel", Map.of(
                        "modelId", "structuredChatClient",
                        "provider", "openai-compatible",
                        "temperature", 0.0,
                        "features", List.of("json-output", "entity-conversion")
                ),
                "stubMode", isStubMode(),
                "memoryEnabled", memoryEnabled
        );
    }

    private boolean isStubMode() {
        return apiKey == null || apiKey.isBlank() || "demo".equals(apiKey);
    }

    // -----------------------------------------------------------------------
    // Stub responses for demo mode
    // -----------------------------------------------------------------------

    private Map<String, Object> stubToolResponse(String message) {
        return Map.of(
                "response", stubHeader("Function Calling") +
                    "Available tools: getCurrentWeather, getForecast, compareCities\n" +
                    "In real mode, the LLM would call these tools based on your message.\n" +
                    "Your message: " + message.substring(0, Math.min(80, message.length())),
                "toolsAvailable", List.of("getCurrentWeather", "getForecast", "compareCities"),
                "processingTimeMs", 0,
                "stubMode", true,
                "timestamp", Instant.now().toString()
        );
    }

    private StructuredOutput.Classification stubClassification(String text) {
        return new StructuredOutput.Classification(
                "demo", "NEUTRAL", 0.99,
                List.of("demo", "stub", "test"),
                false
        );
    }

    private StructuredOutput.WeatherReport stubWeatherReport(String city) {
        return new StructuredOutput.WeatherReport(
                city, "DemoLand", 22.5, "C,", "Partly Cloudy",
                65, 12.3, "km/h",
                List.of(
                        new StructuredOutput.DayForecast("2025-06-21", 24.0, 18.0, "Sunny", 10),
                        new StructuredOutput.DayForecast("2025-06-22", 21.0, 16.0, "Cloudy", 40),
                        new StructuredOutput.DayForecast("2025-06-23", 19.0, 14.0, "Light Rain", 70)
                ),
                "This is a stub weather report. Set a real API key for actual data."
        );
    }

    private Map<String, Object> stubAdvisorResponse(String message, String userId) {
        return Map.of(
                "response", stubHeader("Advisors") +
                    "Advisors would process: CustomLoggingAdvisor (rate limiter + logger) + SimpleLoggerAdvisor\n" +
                    "User: " + userId + "\nMessage preview: " + message.substring(0, Math.min(50, message.length())),
                "advisorsApplied", List.of("CustomLoggingAdvisor", "SimpleLoggerAdvisor"),
                "userId", userId,
                "rateLimitRemaining", 25,
                "processingTimeMs", 0,
                "stubMode", true,
                "timestamp", Instant.now().toString()
        );
    }

    private Map<String, Object> stubRoutingResponse(String message, String complexity) {
        return Map.of(
                "response", stubHeader("Model Routing") +
                    "Would route to: " + complexity + "\n" +
                    "Message preview: " + message.substring(0, Math.min(50, message.length())),
                "routedTo", complexity + "Model",
                "processingTimeMs", 0,
                "stubMode", true,
                "timestamp", Instant.now().toString()
        );
    }

    private Map<String, Object> stubMemoryResponse(String message, String userId, boolean reset) {
        return Map.of(
                "response", stubHeader("Chat Memory") +
                    "User: " + userId + "\nReset: " + reset + "\n" +
                    "Message: " + message.substring(0, Math.min(80, message.length())) + "\n\n" +
                    "In real mode, past conversation turns would be included as context.",
                "userId", userId,
                "historySize", reset ? 1 : 5,
                "memoryEnabled", memoryEnabled,
                "processingTimeMs", 0,
                "stubMode", true,
                "timestamp", Instant.now().toString()
        );
    }

    private Map<String, Object> stubTemplateResponse(Map<String, Object> params) {
        return Map.of(
                "response", stubHeader("Prompt Templates") +
                    "Template params: " + params + "\n" +
                    "In real mode, the template variables would be substituted into the prompt.",
                "templateParams", params,
                "processingTimeMs", 0,
                "stubMode", true,
                "timestamp", Instant.now().toString()
        );
    }

    private String stubHeader(String feature) {
        return "╔═══════════════════════════════════════════════╗\n" +
               "║  STUB: %-39s║\n" +
               "╠═══════════════════════════════════════════════╣\n".formatted(feature);
    }
}
