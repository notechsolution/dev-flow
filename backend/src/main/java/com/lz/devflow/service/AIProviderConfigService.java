package com.lz.devflow.service;

import com.lz.devflow.dto.AIProviderConfigDTO;
import com.lz.devflow.entity.AIProviderConfig;

import java.util.List;

/**
 * Service for managing AI Provider configurations
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
     * Create new provider configuration
     */
    AIProviderConfigDTO createProvider(AIProviderConfigDTO providerDTO);
    
    /**
     * Update provider configuration
     */
    AIProviderConfigDTO updateProvider(String id, AIProviderConfigDTO providerDTO);
    
    /**
     * Enable/Disable provider
     */
    AIProviderConfigDTO toggleProvider(String id, boolean enabled);
    
    /**
     * Delete provider configuration
     */
    void deleteProvider(String id);
    
    /**
     * Initialize default provider configurations if not exist
     */
    void initializeDefaultProviders();
}
