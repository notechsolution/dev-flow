package com.lz.devflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI Configuration for multiple providers
 * Supports switching between different AI providers (Qwen/DashScope, Ollama, etc.)
 * 
 * Priority:
 * 1. If ai.provider=ollama: Create OllamaChatModel (manual config)
 * 2. Otherwise: Use DashScopeChatModel (auto-config from spring-ai-alibaba)
 */
@Configuration
@AutoConfigureBefore(name = "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration")
public class AIConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AIConfiguration.class);

    /**
     * Configuration for Ollama provider - Manual Configuration with High Priority
     * Activated when ai.provider=ollama
     * 
     * This bean is created BEFORE spring-ai-alibaba's auto-configuration
     * to prevent DashScopeChatModel from being created.
     */
    @Configuration
    @ConditionalOnProperty(name = "ai.provider", havingValue = "ollama")
    public static class OllamaConfiguration {
        
        @Bean
        @Primary
        public ChatModel ollamaChatModel(
                @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
                @Value("${spring.ai.ollama.chat.options.model:llama2}") String model,
                @Value("${spring.ai.ollama.chat.options.temperature:0.7}") Double temperature) {
            
            logger.info("Manually configuring Ollama ChatModel with base-url: {}, model: {}", baseUrl, model);
            
            OllamaApi ollamaApi = OllamaApi.builder().baseUrl(baseUrl).build();
            
            OllamaOptions options = OllamaOptions.builder()
                    .model(model)
                    .temperature(temperature)
                    .build();
            
            return OllamaChatModel.builder()
                    .ollamaApi(ollamaApi)
                    .defaultOptions(options)
                    .build();
        }
    }

    /**
     * Configuration for DashScope (Qwen) provider
     * Activated when ai.provider=qwen (default) or not set
     * 
     * DashScopeChatModel will be auto-configured by spring-ai-alibaba-starter
     * only if no other ChatModel bean exists.
     */
    @Configuration
    @ConditionalOnProperty(name = "ai.provider", havingValue = "qwen", matchIfMissing = true)
    @ConditionalOnMissingBean(ChatModel.class)
    public static class QwenConfiguration {
        
        public QwenConfiguration() {
            logger.info("Using DashScope (Qwen) ChatModel from spring-ai-alibaba auto-configuration");
        }
    }
}
