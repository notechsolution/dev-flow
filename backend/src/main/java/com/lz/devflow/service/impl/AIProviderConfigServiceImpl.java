package com.lz.devflow.service.impl;

import com.lz.devflow.dto.AIProviderConfigDTO;
import com.lz.devflow.entity.AIProviderConfig;
import com.lz.devflow.repository.AIProviderConfigRepository;
import com.lz.devflow.service.AIProviderConfigService;
import com.lz.devflow.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AI Provider Config Service
 */
@Service
public class AIProviderConfigServiceImpl implements AIProviderConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIProviderConfigServiceImpl.class);
    
    @Autowired
    private AIProviderConfigRepository repository;
    
    @Autowired
    private CryptoUtil cryptoUtil;
    
    @Override
    public List<AIProviderConfigDTO> getAllProviders() {
        List<AIProviderConfig> providers = repository.findAll();
        return providers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AIProviderConfigDTO> getEnabledProviders() {
        List<AIProviderConfig> providers = repository.findByEnabledTrue();
        return providers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AIProviderConfigDTO getProviderById(String id) {
        AIProviderConfig provider = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        return convertToDTO(provider);
    }
    
    @Override
    public AIProviderConfigDTO getProviderByName(String providerName) {
        AIProviderConfig provider = repository.findByProvider(providerName)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerName));
        return convertToDTO(provider);
    }
    
    @Override
    public AIProviderConfigDTO createProvider(AIProviderConfigDTO providerDTO) {
        // Check if provider already exists
        if (repository.existsByProvider(providerDTO.getProvider())) {
            throw new RuntimeException("Provider already exists: " + providerDTO.getProvider());
        }
        
        AIProviderConfig provider = convertToEntity(providerDTO);
        provider.setCreatedAt(LocalDateTime.now());
        provider.setUpdatedAt(LocalDateTime.now());
        provider.setCreatedBy(getCurrentUserId());
        provider.setUpdatedBy(getCurrentUserId());
        
        // Encrypt API key if provided
        if (providerDTO.getApiKey() != null && !providerDTO.getApiKey().isEmpty()) {
            provider.setApiKey(cryptoUtil.encrypt(providerDTO.getApiKey()));
        }
        
        AIProviderConfig saved = repository.save(provider);
        logger.info("Created AI provider configuration: {}", saved.getProvider());
        
        return convertToDTO(saved);
    }
    
    @Override
    public AIProviderConfigDTO updateProvider(String id, AIProviderConfigDTO providerDTO) {
        AIProviderConfig existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        
        // Update fields
        existing.setDisplayName(providerDTO.getDisplayName());
        existing.setDescription(providerDTO.getDescription());
        existing.setEnabled(providerDTO.isEnabled());
        existing.setBaseUrl(providerDTO.getBaseUrl());
        existing.setDefaultModel(providerDTO.getDefaultModel());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUserId());
        
        // Update API key if provided and not masked
        if (providerDTO.getApiKey() != null && 
            !providerDTO.getApiKey().isEmpty() && 
            !providerDTO.getApiKey().contains("***")) {
            existing.setApiKey(cryptoUtil.encrypt(providerDTO.getApiKey()));
        }
        
        // Update models
        if (providerDTO.getModels() != null) {
            List<AIProviderConfig.ModelConfig> models = providerDTO.getModels().stream()
                    .map(dto -> new AIProviderConfig.ModelConfig(
                            dto.getModelId(),
                            dto.getModelName(),
                            dto.getDescription(),
                            dto.isEnabled()))
                    .collect(Collectors.toList());
            existing.setModels(models);
        }
        
        AIProviderConfig saved = repository.save(existing);
        logger.info("Updated AI provider configuration: {}", saved.getProvider());
        
        return convertToDTO(saved);
    }
    
    @Override
    public AIProviderConfigDTO toggleProvider(String id, boolean enabled) {
        AIProviderConfig provider = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        
        provider.setEnabled(enabled);
        provider.setUpdatedAt(LocalDateTime.now());
        provider.setUpdatedBy(getCurrentUserId());
        
        AIProviderConfig saved = repository.save(provider);
        logger.info("Toggled AI provider {}: enabled={}", saved.getProvider(), enabled);
        
        return convertToDTO(saved);
    }
    
    @Override
    public void deleteProvider(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Provider not found: " + id);
        }
        
        repository.deleteById(id);
        logger.info("Deleted AI provider configuration: {}", id);
    }
    
    @Override
    public void initializeDefaultProviders() {
        logger.info("Initializing default AI provider configurations...");
        
        // DashScope
        if (!repository.existsByProvider("dashscope")) {
            AIProviderConfig dashscope = new AIProviderConfig();
            dashscope.setProvider("dashscope");
            dashscope.setDisplayName("DashScope (百炼)");
            dashscope.setDescription("阿里云通义千问大模型");
            dashscope.setEnabled(true);
            dashscope.setBaseUrl("https://dashscope.aliyuncs.com/api/v1");
            dashscope.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("qwen3-code-plus", "通义千问代码-Plus", "代码生成专用模型", true),
                    new AIProviderConfig.ModelConfig("qwen3-max", "通义千问-Max", "最强性能模型", true)
            ));
            dashscope.setDefaultModel("qwen3-max");
            dashscope.setCreatedAt(LocalDateTime.now());
            dashscope.setUpdatedAt(LocalDateTime.now());
            repository.save(dashscope);
            logger.info("Initialized DashScope provider configuration");
        }
        
        // Ollama
        if (!repository.existsByProvider("ollama")) {
            AIProviderConfig ollama = new AIProviderConfig();
            ollama.setProvider("ollama");
            ollama.setDisplayName("Ollama");
            ollama.setDescription("本地运行的开源大模型");
            ollama.setEnabled(true);
            ollama.setBaseUrl("http://localhost:11434");
            ollama.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("qwen3:8b", "Qwen 3 8B", "阿里云百炼大模型", true),
                    new AIProviderConfig.ModelConfig("qwen3:32b", "Qwen 3 32B", "高性能开源模型", true)
            ));
            ollama.setDefaultModel("qwen3:8b");
            ollama.setCreatedAt(LocalDateTime.now());
            ollama.setUpdatedAt(LocalDateTime.now());
            repository.save(ollama);
            logger.info("Initialized Ollama provider configuration");
        }
        
        // OpenAI
        if (!repository.existsByProvider("openai")) {
            AIProviderConfig openai = new AIProviderConfig();
            openai.setProvider("openai");
            openai.setDisplayName("OpenAI");
            openai.setDescription("OpenAI GPT模型");
            openai.setEnabled(true);
            openai.setBaseUrl("https://api.openai.com/v1");
            openai.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("gpt-3.5-turbo", "GPT-3.5 Turbo", "快速且经济的模型", true),
                    new AIProviderConfig.ModelConfig("gpt-4", "GPT-4", "最强性能模型", true),
                    new AIProviderConfig.ModelConfig("gpt-4-turbo", "GPT-4 Turbo", "更快的GPT-4", true)
            ));
            openai.setDefaultModel("gpt-3.5-turbo");
            openai.setCreatedAt(LocalDateTime.now());
            openai.setUpdatedAt(LocalDateTime.now());
            repository.save(openai);
            logger.info("Initialized OpenAI provider configuration");
        }
        
        logger.info("Default AI provider configurations initialized");
    }
    
    /**
     * Convert entity to DTO (mask API key)
     */
    private AIProviderConfigDTO convertToDTO(AIProviderConfig entity) {
        AIProviderConfigDTO dto = new AIProviderConfigDTO();
        dto.setId(entity.getId());
        dto.setProvider(entity.getProvider());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setDefaultModel(entity.getDefaultModel());
        
        // Mask API key
        if (entity.getApiKey() != null && !entity.getApiKey().isEmpty()) {
            dto.setApiKey("***" + entity.getApiKey().substring(Math.max(0, entity.getApiKey().length() - 4)));
        }
        
        // Convert models
        if (entity.getModels() != null) {
            List<AIProviderConfigDTO.ModelConfigDTO> modelDTOs = entity.getModels().stream()
                    .map(model -> new AIProviderConfigDTO.ModelConfigDTO(
                            model.getModelId(),
                            model.getModelName(),
                            model.getDescription(),
                            model.isEnabled()))
                    .collect(Collectors.toList());
            dto.setModels(modelDTOs);
        }
        
        return dto;
    }
    
    /**
     * Convert DTO to entity
     */
    private AIProviderConfig convertToEntity(AIProviderConfigDTO dto) {
        AIProviderConfig entity = new AIProviderConfig();
        entity.setProvider(dto.getProvider());
        entity.setDisplayName(dto.getDisplayName());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.isEnabled());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setDefaultModel(dto.getDefaultModel());
        
        // Convert models
        if (dto.getModels() != null) {
            List<AIProviderConfig.ModelConfig> models = dto.getModels().stream()
                    .map(modelDTO -> new AIProviderConfig.ModelConfig(
                            modelDTO.getModelId(),
                            modelDTO.getModelName(),
                            modelDTO.getDescription(),
                            modelDTO.isEnabled()))
                    .collect(Collectors.toList());
            entity.setModels(models);
        }
        
        return entity;
    }
    
    /**
     * Get current user ID from security context
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            }
        } catch (Exception e) {
            logger.debug("Could not get current user ID from security context", e);
        }
        return "system";
    }
}
