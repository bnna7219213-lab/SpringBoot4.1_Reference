package com.example.aiadv.model;

import java.util.List;

/**
 * Record types for structured output conversion.
 *
 * <p>Spring AI 2.0 can convert LLM responses directly into typed Java objects
 * using {@code ChatClient.Entity()} or {@code beanOutputConverter()}.
 * The framework sends a JSON schema hint to the LLM and deserializes
 * the response into the target type.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   StructuredOutput.WeatherReport report = chatClient.prompt()
 *       .user("What's the weather in Paris?")
 *       .call()
 *       .entity(StructuredOutput.WeatherReport.class);
 * </pre>
 */
public class StructuredOutput {

    /**
     * A weather report with structured fields.
     * Demonstrates nesting and collections.
     */
    public record WeatherReport(
            String city,
            String country,
            double temperature,
            String unit,
            String conditions,
            int humidity,
            double windSpeed,
            String windUnit,
            List<DayForecast> forecast,
            String summary
    ) {}

    /**
     * A single day in a multi-day forecast.
     */
    public record DayForecast(
            String date,
            double highTemp,
            double lowTemp,
            String conditions,
            int chanceOfRain
    ) {}

    /**
     * Classification result for content moderation or intent detection.
     * Demonstrates enums and simple types.
     */
    public record Classification(
            String category,
            String sentiment,
            double confidence,
            List<String> keywords,
            boolean requiresReview
    ) {}

    /**
     * Sentiment values for Classification.
     */
    public enum Sentiment {
        POSITIVE, NEGATIVE, NEUTRAL, MIXED
    }

    /**
     * Product information extracted from unstructured text.
     * Shows how Spring AI can extract entities into records.
     */
    public record ProductInfo(
            String name,
            String brand,
            String category,
            double price,
            String currency,
            int reviewCount,
            double averageRating,
            List<String> pros,
            List<String> cons,
            String recommendedFor
    ) {}

    /**
     * Code review output — useful when asking the LLM to analyze code.
     */
    public record CodeReview(
            String language,
            int overallScore,
            List<String> issues,
            List<String> suggestions,
            List<String> securityConcerns,
            String refactoredCode,
            String summary
    ) {}

    /**
     * Wraps any structured response with metadata.
     */
    public record StructuredResponse<T>(
            T data,
            String modelUsed,
            long processingTimeMs,
            List<String> toolsInvoked
    ) {}
}
