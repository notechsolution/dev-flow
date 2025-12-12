package com.lz.devflow.dto;

import java.util.List;

/**
 * DTO for AI Provider Configuration
 */
public class AIProviderConfigDTO {
    
    private String id;
    private String provider;
    private String displayName;
    private String description;
    private boolean enabled;
    private String apiKey; // For display, will be masked
    private String baseUrl;
    private List<ModelConfigDTO> models;
    private String defaultModel;
    
    public static class ModelConfigDTO {
        private String modelId;
        private String modelName;
        private String description;
        private boolean enabled;
        
        // Constructors
        public ModelConfigDTO() {}
        
        public ModelConfigDTO(String modelId, String modelName, String description, boolean enabled) {
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
    public AIProviderConfigDTO() {}
    
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
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public List<ModelConfigDTO> getModels() {
        return models;
    }
    
    public void setModels(List<ModelConfigDTO> models) {
        this.models = models;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
    
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }
}
