package com.example.aiadv.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.aiadv.advisor.CustomLoggingAdvisor;
import com.example.aiadv.model.StructuredOutput;
import com.example.aiadv.service.AdvancedChatService;

import reactor.core.publisher.Flux;

/**
 * REST controller for advanced Spring AI 2.0 demo endpoints.
 *
 * <p>Endpoints:</p>
 * <pre>
 *   POST   /api/advanced/tools                   — Chat with function calling
 *   POST   /api/advanced/structured/classify     — Text classification (structured output)
 *   POST   /api/advanced/structured/weather      — Structured weather report
 *   POST   /api/advanced/advisors                — Chat through custom advisor pipeline
 *   POST   /api/advanced/route                   — Multi-model routing
 *   POST   /api/advanced/memory                  — Multi-turn chat with memory
 *   POST   /api/advanced/template                — Prompt template with variable binding
 *   GET    /api/advanced/model-info              — Model configuration info
 *   GET    /api/advanced/tools                   — List available tool functions
 *   GET    /api/advanced/rate-limit              — Rate limit status for current user
 *   GET    /api/advanced/stream                  — SSE streaming with tools
 * </pre>
 */
@RestController
@RequestMapping("/api/advanced")
public class AdvancedChatController {

    private static final Logger log = LoggerFactory.getLogger(AdvancedChatController.class);

    private final AdvancedChatService chatService;

    public AdvancedChatController(AdvancedChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Extract userId from header or generate a default.
     */
    private String resolveUserId(Map<String, String> headers) {
        String userId = headers.getOrDefault("x-user-id", "anonymous");
        return userId.isBlank() ? "anonymous" : userId;
    }

    // -----------------------------------------------------------------------
    // 1. Function Calling / Tool Use
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/tools
     *
     * <p>Chat with automatic function calling. If the user's message relates to
     * weather, the LLM will call the WeatherTool to fetch real data.</p>
     *
     * <p>Request: {"message": "What's the weather in Tokyo?"}</p>
     * <p>Header: X-User-Id: your-user-id (optional, for rate limiting)</p>
     */
    @PostMapping("/tools")
    public ResponseEntity<Map<String, Object>> chatWithTools(
            @RequestBody Map<String, String> body,
            @RequestHeader Map<String, String> headers) {

        String message = body.getOrDefault("message", "");
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Field 'message' is required.",
                    "example", Map.of("message", "What's the weather in London?")
            ));
        }

        String userId = resolveUserId(headers);
        log.info("POST /api/advanced/tools userId={} messageLength={}", userId, message.length());

        return ResponseEntity.ok(chatService.chatWithTools(message, userId));
    }

    // -----------------------------------------------------------------------
    // 2. Structured Output
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/structured/classify
     *
     * <p>Classify text and return structured data typed as a Java record.</p>
     *
     * <p>Request: {"text": "The new product is amazing! Best purchase this year."}</p>
     */
    @PostMapping("/structured/classify")
    public ResponseEntity<StructuredOutput.Classification> classifyText(
            @RequestBody Map<String, String> body) {

        String text = body.getOrDefault("text", "");
        if (text.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new StructuredOutput.Classification("error", "NEUTRAL", 0.0,
                            List.of(), true));
        }

        log.info("POST /api/advanced/structured/classify textLength={}", text.length());
        return ResponseEntity.ok(chatService.classifyText(text));
    }

    /**
     * POST /api/advanced/structured/weather
     *
     * <p>Generate a structured weather report as a typed record.</p>
     *
     * <p>Request: {"city": "Berlin"}</p>
     */
    @PostMapping("/structured/weather")
    public ResponseEntity<StructuredOutput.WeatherReport> structuredWeather(
            @RequestBody Map<String, String> body) {

        String city = body.getOrDefault("city", "");
        if (city.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new StructuredOutput.WeatherReport("unknown", "?", 0, "C", "?", 0, 0, "?",
                            List.of(), "City is required."));
        }

        log.info("POST /api/advanced/structured/weather city={}", city);
        return ResponseEntity.ok(chatService.getStructuredWeatherReport(city));
    }

    // -----------------------------------------------------------------------
    // 3. Custom Advisors
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/advisors
     *
     * <p>Demonstrates the custom advisor pipeline (logging + rate limiting).</p>
     *
     * <p>Request: {"message": "Explain quantum entanglement briefly"}</p>
     * <p>Header: X-User-Id: your-user-id</p>
     */
    @PostMapping("/advisors")
    public ResponseEntity<Map<String, Object>> chatWithAdvisors(
            @RequestBody Map<String, String> body,
            @RequestHeader Map<String, String> headers) {

        String message = body.getOrDefault("message", "");
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Field 'message' is required."));
        }

        String userId = resolveUserId(headers);
        log.info("POST /api/advanced/advisors userId={}", userId);

        try {
            return ResponseEntity.ok(chatService.chatWithAdvisors(message, userId));
        } catch (CustomLoggingAdvisor.RateLimitExceededException e) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", e.getMessage(),
                    "code", "RATE_LIMITED"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "BAD_REQUEST"
            ));
        }
    }

    // -----------------------------------------------------------------------
    // 4. Multi-Model Routing
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/route
     *
     * <p>Routes to the appropriate model based on complexity.</p>
     *
     * <p>Request: {"message": "What is 2+2?", "complexity": "fast"}</p>
     * <p>Complexity values: "fast", "complex", "default"</p>
     */
    @PostMapping("/route")
    public ResponseEntity<Map<String, Object>> chatWithRouting(@RequestBody Map<String, Object> body) {
        String message = (String) body.getOrDefault("message", "");
        String complexity = (String) body.getOrDefault("complexity", "default");

        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Field 'message' is required.",
                    "complexityOptions", List.of("fast", "complex", "default")
            ));
        }

        log.info("POST /api/advanced/route complexity={} messageLength={}", complexity, message.length());
        return ResponseEntity.ok(chatService.chatWithRouting(message, complexity));
    }

    // -----------------------------------------------------------------------
    // 5. Chat with Memory
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/memory
     *
     * <p>Multi-turn chat using in-memory conversation history.</p>
     *
     * <p>Request: {"message": "My name is Alice", "userId": "user-alice", "reset": true}</p>
     * <p>Then: {"message": "What's my name?", "userId": "user-alice"}</p>
     */
    @PostMapping("/memory")
    public ResponseEntity<Map<String, Object>> chatWithMemory(@RequestBody Map<String, Object> body) {
        String message = (String) body.getOrDefault("message", "");
        String userId = (String) body.getOrDefault("userId", "default");
        boolean reset = Boolean.parseBoolean(String.valueOf(body.getOrDefault("reset", "false")));

        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Fields 'message' and 'userId' are required.",
                    "example", Map.of(
                            "message", "Hello, my name is Bob",
                            "userId", "user-bob",
                            "reset", true
                    )
            ));
        }

        log.info("POST /api/advanced/memory userId={} reset={} messageLength={}",
                userId, reset, message.length());

        return ResponseEntity.ok(chatService.chatWithMemory(message, userId, reset));
    }

    // -----------------------------------------------------------------------
    // 6. Prompt Template
    // -----------------------------------------------------------------------

    /**
     * POST /api/advanced/template
     *
     * <p>Chat using a parameterized prompt template.</p>
     *
     * <p>Request:</p>
     * <pre>
     * {
     *   "systemPrompt": "You are a math tutor.",
     *   "context": "The student is in grade 8.",
     *   "style": "step-by-step",
     *   "userMessage": "Solve for x: 2x + 5 = 15"
     * }
     * </pre>
     */
    @PostMapping("/template")
    public ResponseEntity<Map<String, Object>> chatWithTemplate(@RequestBody Map<String, Object> params) {
        String userMessage = (String) params.getOrDefault("userMessage", "Hello!");

        if (userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Field 'userMessage' is required."));
        }

        log.info("POST /api/advanced/template");
        return ResponseEntity.ok(chatService.chatWithPromptTemplate(params));
    }

    // -----------------------------------------------------------------------
    // Informational Endpoints
    // -----------------------------------------------------------------------

    /**
     * GET /api/advanced/model-info
     *
     * <p>Returns configuration of all model variants.</p>
     */
    @GetMapping("/model-info")
    public ResponseEntity<Map<String, Object>> modelInfo() {
        return ResponseEntity.ok(chatService.getModelInfo());
    }

    /**
     * GET /api/advanced/tools
     *
     * <p>Lists all tool functions available for function calling.</p>
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        return ResponseEntity.ok(Map.of(
                "tools", chatService.getAvailableTools(),
                "description", "These tools can be invoked by the LLM via function calling.",
                "registeredBean", "WeatherTool"
        ));
    }

    /**
     * GET /api/advanced/rate-limit
     *
     * <p>Returns rate limit status for the requesting user.</p>
     *
     * <p>Header: X-User-Id: your-user-id</p>
     */
    @GetMapping("/rate-limit")
    public ResponseEntity<Map<String, Object>> rateLimitStatus(
            @RequestHeader Map<String, String> headers) {
        String userId = resolveUserId(headers);
        return ResponseEntity.ok(CustomLoggingAdvisor.getRateLimitStats(userId));
    }

    // -----------------------------------------------------------------------
    // Streaming
    // -----------------------------------------------------------------------

    /**
     * GET /api/advanced/stream
     *
     * <p>SSE streaming endpoint. Streams the LLM response token by token.</p>
     *
     * <p>Query params: message (required), userId (optional)</p>
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam("message") String message,
            @RequestHeader Map<String, String> headers) {

        String userId = resolveUserId(headers);
        log.info("GET /api/advanced/stream userId={} messageLength={}", userId, message.length());

        if (message.isBlank()) {
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event().name("error")
                        .data(Map.of("error", "Query parameter 'message' is required")));
                errorEmitter.complete();
            } catch (Exception e) {
                errorEmitter.completeWithError(e);
            }
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(120_000L);

        // Use the service for streaming via the default chat client
        // Note: In a real app, you'd have a dedicated streaming service method
        Flux<String> flux = chatService.chatWithTools(message, userId)
                .get("response") instanceof String response
                        ? Flux.just(response.split("(?<=\\s)")[0]) // Simplified for demo
                        : Flux.just("Streaming requires a real API key.");

        StringBuilder fullResponse = new StringBuilder();

        flux.subscribe(
                token -> {
                    try {
                        fullResponse.append(token);
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(token)
                                .id(String.valueOf(fullResponse.length())));
                    } catch (Exception e) {
                        log.error("SSE send error", e);
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("Stream error", error);
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("complete")
                                .data(Map.of(
                                        "fullResponse", fullResponse.toString(),
                                        "userId", userId,
                                        "tokenCount", fullResponse.toString().split("\\s+").length
                                )));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("SSE complete error", e);
                        emitter.completeWithError(e);
                    }
                }
        );

        return emitter;
    }
}
