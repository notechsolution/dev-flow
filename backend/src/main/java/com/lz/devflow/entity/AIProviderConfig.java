package com.lz.devflow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Provider Configuration Entity
 * Stores configuration for AI providers (DashScope, Ollama, OpenAI)
 * API keys are configured in application.yml and injected at startup
 */
@Document(collection = "ai_provider_configs")
public class AIProviderConfig {
    
    @Id
    private String id;
    
    private String provider; // dashscope, ollama, openai
    
    private String displayName; // Display name for UI
    
    private String description;
    
    private boolean enabled; // Whether this provider is enabled
    
    // API key removed - configured in application.yml
    
    private String baseUrl; // Base URL for API calls (read-only from config)
    
    private List<ModelConfig> models; // List of supported models
    
    private String defaultModel; // Default model to use
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    // Inner class for model configuration
    public static class ModelConfig {
        private String modelId;
        private String modelName;
        private String description;
        private boolean enabled;
        
        // Constructors
        public ModelConfig() {}
        
        public ModelConfig(String modelId, String modelName, String description, boolean enabled) {
            this.modelId = modelId;
            this.modelName = modelName;
            this.description = description;
            this.enabled = enabled;
        }
        
        // Getters and Setters
        public String getModelId() {
            return modelId;
        }
        
        public void setModelId(String modelId) {
            this.modelId = modelId;
        }
        
        public String getModelName() {
            return modelName;
        }
        
        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    // Constructors
    public AIProviderConfig() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public List<ModelConfig> getModels() {
        return models;
    }
    
    public void setModels(List<ModelConfig> models) {
        this.models = models;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
    
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
