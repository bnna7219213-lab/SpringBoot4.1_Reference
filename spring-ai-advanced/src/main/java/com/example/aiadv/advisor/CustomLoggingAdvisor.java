package com.example.aiadv.advisor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisorChain;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

/**
 * Custom advisor that logs requests/responses and implements basic rate limiting.
 *
 * <p>Advisors are the Spring AI 2.0 mechanism for cross-cutting concerns in the
 * chat request pipeline. They wrap around the core processing logic, similar to
 * servlet filters or Spring interceptors.</p>
 *
 * <p>This advisor demonstrates:</p>
 * <ul>
 *   <li>Pre-processing: Log the incoming user message and check rate limits</li>
 *   <li>Post-processing: Log the response, timing, and token counts</li>
 *   <li>Rate limiting: Simple per-user sliding window rate limiter</li>
 *   <li>Ordering: Control where this advisor sits in the chain via {@link Ordered}</li>
 * </ul>
 *
 * <p>Usage in ChatClient:</p>
 * <pre>
 *   chatClient.prompt()
 *       .user("My question")
 *       .advisors(new CustomLoggingAdvisor("user-123"))
 *       .call()
 * </pre>
 */
public class CustomLoggingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(CustomLoggingAdvisor.class);

    private final String userId;
    private final boolean logRequests;
    private final boolean logResponses;
    private final int order;

    /**
     * Per-user rate limit tracking.
     * userId -> list of request timestamps (epoch seconds)
     */
    private static final ConcurrentHashMap<String, java.util.List<Long>> requestTimestamps
            = new ConcurrentHashMap<>();

    /** Maximum requests allowed per user per minute */
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    /** Max tokens per request to prevent abuse */
    private static final int MAX_MESSAGE_LENGTH = 10000;

    /**
     * Total request counter (across all users).
     */
    private static final AtomicInteger totalRequests = new AtomicInteger(0);

    public CustomLoggingAdvisor(String userId) {
        this(userId, true, true, Ordered.HIGHEST_PRECEDENCE + 100);
    }

    public CustomLoggingAdvisor(String userId, boolean logRequests, boolean logResponses, int order) {
        this.userId = userId;
        this.logRequests = logRequests;
        this.logResponses = logResponses;
        this.order = order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, BaseAdvisorChain advisorChain) {
        int requestNum = totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        // Store start time in request context for after() to use
        Map<String, Object> context = new HashMap<>(chatClientRequest.context());
        context.put("startTime", startTime);
        context.put("requestNumber", requestNum);

        String userMessage = extractUserMessage(chatClientRequest);

        if (logRequests) {
            log.info("[Request #{}] user={} messageLength={} messagePreview={}",
                    requestNum, userId,
                    userMessage.length(),
                    truncate(userMessage, 100));
        }

        // Rate limit check
        if (!checkRateLimit(userId)) {
            log.warn("[Request #{}] RATE LIMITED user={}", requestNum, userId);
            throw new RateLimitExceededException(
                    "Rate limit exceeded for user " + userId +
                    ". Max " + MAX_REQUESTS_PER_MINUTE + " requests/minute.");
        }

        // Message length check
        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            log.warn("[Request #{}] MESSAGE TOO LONG user={} length={}",
                    requestNum, userId, userMessage.length());
            throw new IllegalArgumentException(
                    "Message exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters");
        }

        return ChatClientRequest.builder()
                .from(chatClientRequest)
                .context(context)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, BaseAdvisorChain advisorChain) {
        long endTime = System.currentTimeMillis();
        long startTime = (Long) chatClientResponse.context().getOrDefault("startTime", endTime);
        int requestNum = (Integer) chatClientResponse.context().getOrDefault("requestNumber", -1);
        long duration = endTime - startTime;

        String responseContent = chatClientResponse.chatResponse() != null
                && chatClientResponse.chatResponse().getResult() != null
                ? chatClientResponse.chatResponse().getResult().getOutput().getText()
                : "[no response]";

        if (logResponses) {
            log.info("[Request #{}] completed user={} duration={}ms responseLength={} responsePreview={}",
                    requestNum, userId, duration,
                    responseContent.length(),
                    truncate(responseContent, 150));
        }

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(
            Flux<ChatClientResponse> flux, BaseAdvisorChain advisorChain) {
        long startTime = System.currentTimeMillis();
        AtomicInteger tokenCount = new AtomicInteger(0);

        return flux
                .doOnNext(response -> tokenCount.incrementAndGet())
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[Stream] completed user={} tokens={} duration={}ms",
                            userId, tokenCount.get(), duration);
                })
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("[Stream] error user={} tokens={} duration={}ms error={}",
                            userId, tokenCount.get(), duration, e.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Simple sliding-window rate limiter. Returns true if request is allowed.
     */
    private boolean checkRateLimit(String userId) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - 60; // 1 minute window

        var timestamps = requestTimestamps.computeIfAbsent(userId, k -> new java.util.ArrayList<>());

        synchronized (timestamps) {
            timestamps.removeIf(ts -> ts < windowStart);

            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }

    private String extractUserMessage(ChatClientRequest request) {
        try {
            return request.prompt().getContents();
        } catch (Exception e) {
            return "[could not extract message]";
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Get current rate limit stats for a user.
     */
    public static Map<String, Object> getRateLimitStats(String userId) {
        var timestamps = requestTimestamps.getOrDefault(userId, java.util.List.of());
        long now = Instant.now().getEpochSecond();
        long windowStart = now - 60;

        synchronized (timestamps) {
            timestamps.removeIf(ts -> ts < windowStart);
            return Map.of(
                    "userId", userId,
                    "requestsInCurrentWindow", timestamps.size(),
                    "maxRequestsPerMinute", MAX_REQUESTS_PER_MINUTE,
                    "remaining", Math.max(0, MAX_REQUESTS_PER_MINUTE - timestamps.size())
            );
        }
    }

    /**
     * Exception thrown when rate limit is exceeded.
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
