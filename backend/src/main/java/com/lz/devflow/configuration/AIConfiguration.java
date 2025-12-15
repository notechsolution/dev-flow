package com.lz.devflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import java.util.HashMap;
import java.util.Map;

/**
 * AI Configuration for multiple providers
 * Supports switching between different AI providers (Qwen/DashScope, Ollama, OpenAI)
 */
@Configuration
public class AIConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AIConfiguration.class);
    private final Map<String, ChatModel> chatModels;
    private final Map<String, ChatClient> chatClients;
    
    @Value("${provider-default:dashscope}")
    private String defaultProvider;

    // constructor with all chat models
    public AIConfiguration(
            DashScopeChatModel dashScopeChatModel,
            OllamaChatModel ollamaChatModel,
            OpenAiChatModel openAiChatModel) {
        
        this.chatModels = new HashMap<>();
        this.chatClients = new HashMap<>();
        
        // Register DashScope
        if (dashScopeChatModel != null) {
            this.chatModels.put("dashscope", dashScopeChatModel);
            ChatClient dashscopeClient = ChatClient.builder(dashScopeChatModel)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();
            this.chatClients.put("dashscope", dashscopeClient);
            logger.info("Registered ChatModel for provider: dashscope");
        }
        
        // Register Ollama
        if (ollamaChatModel != null) {
            this.chatModels.put("ollama", ollamaChatModel);
            ChatClient ollamaClient = ChatClient.builder(ollamaChatModel)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();
            this.chatClients.put("ollama", ollamaClient);
            logger.info("Registered ChatModel for provider: ollama");
        }
        
        // Register OpenAI
        if (openAiChatModel != null) {
            this.chatModels.put("openai", openAiChatModel);
            ChatClient openaiClient = ChatClient.builder(openAiChatModel)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();
            this.chatClients.put("openai", openaiClient);
            logger.info("Registered ChatModel for provider: openai");
        }
        
        logger.info("AIConfiguration initialized with {} providers", this.chatClients.size());
    }

    public ChatModel getChatModel(String provider) {
        ChatModel model = this.chatModels.get(provider);
        if (model == null) {
            logger.warn("ChatModel not found for provider: {}, using default: {}", provider, defaultProvider);
            return this.chatModels.get(defaultProvider);
        }
        return model;
    }

    public ChatClient getChatClient(String provider) {
        ChatClient client = this.chatClients.get(provider);
        if (client == null) {
            logger.warn("ChatClient not found for provider: {}, using default: {}", provider, defaultProvider);
            return this.chatClients.get(defaultProvider);
        }
        return client;
    }
    
    /**
     * Get ChatModel for specific provider (to be used with model-specific calls)
     */
    public ChatClient getChatClientForModel(String provider, String model) {
        if (model == null || model.trim().isEmpty()) {
            return getChatClient(provider);
        }
        
        // For now, we'll handle model selection in the service layer
        // by passing model info in the prompt or options
        logger.info("Requested ChatClient for provider: {} with model: {}", provider, model);
        return getChatClient(provider);
    }

    public ChatClient getDefaultChatClient() {
        return this.chatClients.get(defaultProvider);
    }
    
    public String getDefaultProvider() {
        return defaultProvider;
    }
    
    public Map<String, ChatClient> getAllChatClients() {
        return new HashMap<>(chatClients);
    }
}
