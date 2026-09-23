package com.example.aibasic.config;

import java.time.LocalDate;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Spring AI 2.0 configuration for the basic demo.
 *
 * <p>This class shows how to:</p>
 * <ul>
 *   <li>Declare an {@link OpenAiApi} bean with explicit base URL and key</li>
 *   <li>Configure an {@link OpenAiChatModel} with retry templates and chat options</li>
 *   <li>Build a {@link ChatClient} with a system prompt</li>
 *   <li>Wire {@link ToolCallingManager} for optional function-calling support</li>
 * </ul>
 *
 * <p>Note: When {@code spring-ai-starter-model-openai} is on the classpath,
 * Spring Boot 4.1 auto-configures most of these beans. We define them explicitly
 * here for educational purposes and to show how customization works.</p>
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Value("${spring.ai.openai.api-key:demo}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com/}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:2048}")
    private int maxTokens;

    /**
     * Build the low-level OpenAiApi client.
     * The base URL can point to any OpenAI-compatible endpoint (OpenAI, DeepSeek, local models).
     */
    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .completionsPath("/v1/chat/completions")
                .build();
    }

    /**
     * Build the OpenAiChatModel with explicit options and retry template.
     * The retry template will retry on transient errors (5xx, timeouts) up to 3 times
     * with exponential backoff.
     */
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        log.info("Initializing OpenAiChatModel — model={}, base-url={}", model, baseUrl);

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatOptions)
                .toolCallingManager(toolCallingManager())
                .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                .build();
    }

    /**
     * ToolCallingManager is required by OpenAiChatModel when tools/function-calling
     * might be invoked. Even without active tools, the model needs it configured.
     */
    @Bean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    /**
     * The primary ChatClient bean — used by {@link com.example.aibasic.service.ChatService}.
     * Configured with a system prompt that sets the assistant's behavior.
     * The prompt template is resolved from classpath:prompts/system-message.st.
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        var systemTemplate = new DefaultResourceLoader()
                .getResource("classpath:prompts/system-message.st");

        return ChatClient.builder(chatModel)
                .defaultSystem(s -> s
                        .text(systemTemplate)
                        .param("date", LocalDate.now().toString())
                        .param("language", "auto"))
                .build();
    }

    /**
     * Example of an alternative ChatClient that could route to a different model/provider
     * (e.g., a local Ollama instance). Would be conditional on having Ollama configured.
     * Uncomment and add the ollama starter dependency to activate.
     */
    // @Bean
    // @ConditionalOnProperty(prefix = "spring.ai.ollama", name = "base-url")
    // public ChatClient ollamaChatClient(OpenAiChatModel ollamaModel) {
    //     return ChatClient.builder(ollamaModel)
    //             .defaultSystem("You are a helpful assistant running locally via Ollama.")
    //             .build();
    // }
}
