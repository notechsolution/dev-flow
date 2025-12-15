package com.lz.devflow.service;

import com.lz.devflow.dto.AIProviderConfigDTO;

import java.util.List;

/**
 * Service for managing AI Provider configurations (Read-Only)
 * Configurations are loaded from application.yml at startup
 */
public interface AIProviderConfigService {
    
    /**
     * Get all AI provider configurations
     */
    List<AIProviderConfigDTO> getAllProviders();
    
    /**
     * Get enabled AI provider configurations
     */
    List<AIProviderConfigDTO> getEnabledProviders();
    
    /**
     * Get specific provider configuration
     */
    AIProviderConfigDTO getProviderById(String id);
    
    /**
     * Get provider configuration by provider name
     */
    AIProviderConfigDTO getProviderByName(String provider);
    
    /**
     * Enable/Disable provider
     */
    AIProviderConfigDTO toggleProvider(String id, boolean enabled);
    
    /**
     * Update provider models and default model
     * Only models and defaultModel can be updated
     */
    AIProviderConfigDTO updateProviderModels(String id, AIProviderConfigDTO providerDTO);
    
    /**
     * Initialize provider configurations from application.yml at startup
     */
    void initializeProvidersFromConfig();
}
