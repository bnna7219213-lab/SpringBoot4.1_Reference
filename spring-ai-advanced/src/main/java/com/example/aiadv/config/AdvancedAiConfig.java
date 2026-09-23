package com.example.aiadv.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Advanced Spring AI 2.0 configuration.
 *
 * <p>Demonstrates:</p>
 * <ul>
 *   <li>Building multiple ChatClients with different system prompts and options</li>
 *   <li>Configuring ChatMemory for multi-turn conversations</li>
 *   <li>Wiring ToolCallingManager for function-calling support</li>
 *   <li>Model routing — different models for simple vs. complex tasks</li>
 *   <li>Retry template with exponential backoff</li>
 * </ul>
 */
@Configuration
public class AdvancedAiConfig {

    private static final Logger log = LoggerFactory.getLogger(AdvancedAiConfig.class);

    @Value("${spring.ai.openai.api-key:demo}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com/}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String defaultModel;

    @Value("${ai.model-routing.fast-model:gpt-4o-mini}")
    private String fastModel;

    @Value("${ai.model-routing.complex-model:gpt-4o}")
    private String complexModel;

    @Value("${ai.memory.enabled:true}")
    private boolean memoryEnabled;

    @Value("${ai.memory.max-messages:20}")
    private int memoryMaxMessages;

    // -----------------------------------------------------------------------
    // Low-level API and model beans
    // -----------------------------------------------------------------------

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .completionsPath("/v1/chat/completions")
                .build();
    }

    @Bean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    @Bean
    public OpenAiChatModel defaultChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(defaultModel)
                .temperature(0.3)
                .maxTokens(4096)
                .build();

        log.info("defaultChatModel: model={}", defaultModel);

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .toolCallingManager(toolCallingManager())
                .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                .build();
    }

    /**
     * A ChatModel optimized for fast/cheap responses.
     * Lower temperature, smaller model.
     */
    @Bean(name = "fastChatModel")
    public OpenAiChatModel fastChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(fastModel)
                .temperature(0.1)
                .maxTokens(1024)
                .build();

        log.info("fastChatModel: model={}", fastModel);

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .toolCallingManager(toolCallingManager())
                .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                .build();
    }

    /**
     * A ChatModel optimized for complex reasoning tasks.
     * Higher capability model with moderate temperature.
     */
    @Bean(name = "complexChatModel")
    public OpenAiChatModel complexChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(complexModel)
                .temperature(0.5)
                .maxTokens(8192)
                .build();

        log.info("complexChatModel: model={}", complexModel);

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .toolCallingManager(toolCallingManager())
                .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                .build();
    }

    // -----------------------------------------------------------------------
    // ChatMemory for multi-turn conversations
    // -----------------------------------------------------------------------

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(memoryMaxMessages)
                .build();
    }

    // -----------------------------------------------------------------------
    // ChatClient beans with different configurations
    // -----------------------------------------------------------------------

    /**
     * Default ChatClient — balanced for general use.
     * Includes memory advisor for multi-turn conversations.
     */
    @Bean
    public ChatClient defaultChatClient(OpenAiChatModel defaultChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(defaultChatModel)
                .defaultSystem(s -> s
                        .text("""
                            You are an advanced AI assistant with access to tools and memory.
                            You can look up weather information, perform structured analysis,
                            and maintain context across multiple turns.
                            Current date: {date}
                            Be concise and precise. When using tools, explain your reasoning.
                            """)
                        .param("date", LocalDate.now().toString()))
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * Fast ChatClient for simple, quick responses.
     * No memory advisor — stateless by design.
     */
    @Bean(name = "fastChatClient")
    public ChatClient fastChatClient(OpenAiChatModel fastChatModel) {
        return ChatClient.builder(fastChatModel)
                .defaultSystem("You give very short, one-sentence answers. Be direct and to the point.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * Complex ChatClient for deep reasoning tasks.
     * Uses the most capable model with chain-of-thought prompting.
     */
    @Bean(name = "complexChatClient")
    public ChatClient complexChatClient(OpenAiChatModel complexChatModel) {
        return ChatClient.builder(complexChatModel)
                .defaultSystem(s -> s
                        .text("""
                            You are a deep reasoning AI assistant.
                            For complex problems, use chain-of-thought reasoning:
                            1. Break the problem into sub-problems
                            2. Solve each sub-problem step by step
                            3. Verify your solution
                            4. Provide a clear final answer
                            Always explain your reasoning clearly.
                            Current date: {date}
                            """)
                        .param("date", LocalDate.now().toString()))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * Structured output ChatClient — optimized for producing valid JSON.
     * Low temperature to minimize randomness.
     */
    @Bean(name = "structuredChatClient")
    public ChatClient structuredChatClient(OpenAiChatModel defaultChatModel) {
        return ChatClient.builder(defaultChatModel)
                .defaultSystem("""
                    You are a structured data extraction assistant.
                    Always respond with valid JSON that matches the requested schema.
                    Do not include markdown formatting, explanations, or extra text.
                    Just return the raw JSON object.
                    """)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
