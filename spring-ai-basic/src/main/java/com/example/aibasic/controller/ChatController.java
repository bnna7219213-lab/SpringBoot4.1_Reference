package com.example.aibasic.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.aibasic.service.ChatService;

import reactor.core.publisher.Flux;

/**
 * REST controller for the basic Spring AI demo endpoints.
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code POST /api/chat} — synchronous chat, returns full response</li>
 *   <li>{@code GET /api/chat/stream} — Server-Sent Events streaming chat</li>
 *   <li>{@code POST /api/chat/template} — chat with prompt template parameters</li>
 *   <li>{@code GET /api/chat/model-info} — current model configuration</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Synchronous chat endpoint.
     *
     * <p>Request body: {@code {"message": "your question here"}}</p>
     * <p>Response: {@code {"response": "AI response here", "stubMode": false}}</p>
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> requestBody) {
        String message = requestBody.getOrDefault("message", "");

        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Field 'message' is required and must not be blank.",
                    "stubMode", chatService.isStubMode()
            ));
        }

        log.info("POST /api/chat — message length={}", message.length());
        String response = chatService.chat(message);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "stubMode", chatService.isStubMode(),
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Streaming chat endpoint using Server-Sent Events.
     *
     * <p>Accepts a query parameter: {@code ?message=your question here}</p>
     * <p>Each token from the AI is emitted as a separate SSE event.</p>
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(org.springframework.web.bind.annotation.RequestParam("message") String message) {
        if (message == null || message.isBlank()) {
            SseEmitter errorEmitter = new SseEmitter(1000L);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("error", "Query parameter 'message' is required")));
                errorEmitter.complete();
            } catch (Exception e) {
                errorEmitter.completeWithError(e);
            }
            return errorEmitter;
        }

        log.info("GET /api/chat/stream — message length={}", message.length());
        SseEmitter emitter = new SseEmitter(60_000L);

        Flux<String> responseStream = chatService.chatStream(message);
        StringBuilder fullResponse = new StringBuilder();

        responseStream.subscribe(
                token -> {
                    try {
                        fullResponse.append(token);
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(token)
                                .id(java.util.UUID.randomUUID().toString()));
                    } catch (Exception e) {
                        log.error("Error sending SSE token", e);
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
                                .data(Map.of("fullResponse", fullResponse.toString())));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Error sending complete event", e);
                        emitter.completeWithError(e);
                    }
                }
        );

        // Handle client disconnect
        emitter.onTimeout(() -> log.warn("SSE emitter timed out"));
        emitter.onCompletion(() -> log.debug("SSE stream completed normally"));
        emitter.onError(e -> log.error("SSE emitter error", e));

        return emitter;
    }

    /**
     * Chat with a named prompt template.
     *
     * <p>Request body example:</p>
     * <pre>
     * {
     *   "templateName": "system-message.st",
     *   "userMessage": "Explain quantum computing in simple terms",
     *   "date": "2025-06-20",
     *   "language": "en"
     * }
     * </pre>
     */
    @PostMapping("/template")
    public ResponseEntity<Map<String, Object>> chatWithTemplate(@RequestBody Map<String, Object> params) {
        log.info("POST /api/chat/template — params={}", params.keySet());
        String response = chatService.chatWithTemplate(params);

        return ResponseEntity.ok(Map.of(
                "response", response,
                "templateUsed", params.getOrDefault("templateName", "system-message.st"),
                "stubMode", chatService.isStubMode(),
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Returns the current model configuration and mode.
     */
    @GetMapping("/model-info")
    public ResponseEntity<Map<String, Object>> modelInfo() {
        return ResponseEntity.ok(chatService.getModelInfo());
    }
}
