package com.lz.devflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
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

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

/**
 * AI Configuration for multiple providers
 * Supports switching between different AI providers (Qwen/DashScope, Ollama, etc.)
 */
@Configuration
public class AIConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AIConfiguration.class);
    private final Map<String, ChatModel> chatModels;
    private final Map<String, ChatClient> chatClients;
    @Value("${ai.provider-default}")
    private String defaultProvider;

    // constructor with all chat models
    public AIConfiguration(
            DashScopeChatModel dashScopeChatModel,
            OllamaChatModel ollamaChatModel,
            OpenAiChatModel openAiChatModel) {
        
        // store these models as map
        this.chatModels = Map.of(
            "dashscope", dashScopeChatModel,
            "ollama", ollamaChatModel,
            "openai", openAiChatModel
        );
        this.chatClients = new HashMap<>();
        this.chatModels.forEach((provider, model) -> {
            ChatClient chatClient = ChatClient.builder(model)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();
            this.chatClients.put(provider, chatClient);
            logger.info("Registered ChatModel for provider: {}, model: {}", provider, model);
        });
    }

    public ChatModel getChatModel(String provider) {
        return this.chatModels.get(provider);
    }

    public ChatClient getChatClient(String provider) {
        return this.chatClients.get(provider);
    }

    public ChatClient getDefaultChatClient() {
        return this.chatClients.get(defaultProvider);
    }
}
