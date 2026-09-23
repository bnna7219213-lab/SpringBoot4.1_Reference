package com.example.aiadv.service;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Example tool for Spring AI function calling.
 *
 * <p>When registered as a Spring bean, the LLM can decide to call these
 * methods based on the tool descriptions. Spring AI routes the tool call
 * request to this bean and returns the result back to the LLM.</p>
 *
 * <p>Usage in ChatClient:</p>
 * <pre>
 *   chatClient.prompt()
 *       .user("What's the weather in Berlin?")
 *       .tools(weatherTool)   // Makes this bean available for function calling
 *       .call()
 * </pre>
 *
 * <p><strong>Note:</strong> These return mock data for demonstration.
 * In production, weather data would come from a real weather API.</p>
 */
@Component
@Description("Get current weather and forecasts for cities around the world. " +
             "Can retrieve temperature, humidity, wind, and forecast data.")
public class WeatherTool implements Function<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    /**
     * Get current weather for a specific city.
     *
     * @param city    The city name (e.g., "London", "Tokyo")
     * @param country Optional country code (e.g., "US", "DE") for disambiguation
     * @param unit    Temperature unit: "celsius" or "fahrenheit" (default: celsius)
     * @return A map with current weather data
     */
    @Tool(description = "Get the current weather conditions for a specific city. " +
                       "Returns temperature, humidity, wind speed, and conditions. " +
                       "Note: Returns mock data for demonstration purposes.")
    public Map<String, Object> getCurrentWeather(
            @ToolParam(description = "The city name (e.g., London, Tokyo, New York)") String city,
            @ToolParam(description = "Optional ISO country code (e.g., US, GB, DE) for disambiguation") String country,
            @ToolParam(description = "Temperature unit: 'celsius' or 'fahrenheit'") String unit) {

        log.info("Tool called: getCurrentWeather(city={}, country={}, unit={})", city, country, unit);

        boolean fahrenheit = "fahrenheit".equalsIgnoreCase(unit);
        double temp = 20.0 + (Math.random() * 15 - 7.5); // 12.5 - 27.5 C
        double tempDisplay = fahrenheit ? temp * 9.0 / 5.0 + 32 : temp;
        String unitLabel = fahrenheit ? "F" : "C";

        return Map.of(
                "city", city,
                "country", country != null ? country : "Unknown",
                "temperature", Math.round(tempDisplay * 10) / 10.0,
                "unit", unitLabel,
                "conditions", getRandomCondition(),
                "humidity", 40 + (int) (Math.random() * 50),
                "windSpeed", Math.round((Math.random() * 20) * 10) / 10.0,
                "windUnit", "km/h",
                "timestamp", java.time.Instant.now().toString()
        );
    }

    /**
     * Get a multi-day forecast for a city.
     *
     * @param city    The city name
     * @param days    Number of forecast days (1-7)
     * @param unit    Temperature unit: "celsius" or "fahrenheit"
     * @return A map with forecast data for the requested period
     */
    @Tool(description = "Get a weather forecast for a city for the next few days. " +
                       "Returns high/low temps, conditions, and rain probability per day. " +
                       "Note: Returns mock data for demonstration purposes.")
    public Map<String, Object> getForecast(
            @ToolParam(description = "The city name") String city,
            @ToolParam(description = "Number of days to forecast (1 to 7)") int days,
            @ToolParam(description = "Temperature unit: 'celsius' or 'fahrenheit'") String unit) {

        int forecastDays = Math.min(Math.max(days, 1), 7);
        log.info("Tool called: getForecast(city={}, days={}, unit={})", city, forecastDays, unit);

        boolean fahrenheit = "fahrenheit".equalsIgnoreCase(unit);
        String unitLabel = fahrenheit ? "F" : "C";

        var forecasts = new java.util.ArrayList<Map<String, Object>>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (int i = 1; i <= forecastDays; i++) {
            double highC = 18.0 + (Math.random() * 12);
            double lowC = highC - 5.0 - (Math.random() * 5);
            double high = fahrenheit ? highC * 9.0 / 5.0 + 32 : highC;
            double low = fahrenheit ? lowC * 9.0 / 5.0 + 32 : lowC;

            forecasts.add(Map.of(
                    "date", today.plusDays(i).toString(),
                    "highTemp", Math.round(high * 10) / 10.0,
                    "lowTemp", Math.round(low * 10) / 10.0,
                    "conditions", getRandomCondition(),
                    "chanceOfRain", (int) (Math.random() * 100)
            ));
        }

        return Map.of(
                "city", city,
                "days", forecastDays,
                "unit", unitLabel,
                "forecasts", forecasts,
                "generatedAt", java.time.Instant.now().toString()
        );
    }

    /**
     * Compare weather between two cities side by side.
     *
     * @param city1 First city name
     * @param city2 Second city name
     * @return Comparative weather data
     */
    @Tool(description = "Compare current weather between two cities side by side. " +
                       "Useful for 'Which city is warmer?' or planning travel.")
    public Map<String, Object> compareCities(
            @ToolParam(description = "First city name") String city1,
            @ToolParam(description = "Second city name") String city2) {

        log.info("Tool called: compareCities({}, {})", city1, city2);

        double temp1 = 15.0 + Math.random() * 20;
        double temp2 = 15.0 + Math.random() * 20;

        return Map.of(
                "city1", Map.of(
                        "name", city1,
                        "temperature", Math.round(temp1 * 10) / 10.0,
                        "unit", "C",
                        "conditions", getRandomCondition(),
                        "humidity", 40 + (int) (Math.random() * 50)
                ),
                "city2", Map.of(
                        "name", city2,
                        "temperature", Math.round(temp2 * 10) / 10.0,
                        "unit", "C",
                        "conditions", getRandomCondition(),
                        "humidity", 40 + (int) (Math.random() * 50)
                ),
                "warmer", temp1 > temp2 ? city1 : city2,
                "temperatureDifference", Math.round(Math.abs(temp1 - temp2) * 10) / 10.0,
                "unit", "C"
        );
    }

    /**
     * Required for Function interface compliance. Not called directly.
     */
    @Override
    public Object apply(Object input) {
        return "WeatherTool is accessed via @Tool annotated methods, not Function.apply()";
    }

    private String getRandomCondition() {
        String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Light Rain",
                "Heavy Rain", "Thunderstorms", "Snow", "Foggy", "Clear", "Windy"};
        return conditions[(int) (Math.random() * conditions.length)];
    }
}
