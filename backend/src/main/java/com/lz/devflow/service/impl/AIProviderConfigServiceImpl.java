package com.lz.devflow.service.impl;

import com.lz.devflow.dto.AIProviderConfigDTO;
import com.lz.devflow.entity.AIProviderConfig;
import com.lz.devflow.repository.AIProviderConfigRepository;
import com.lz.devflow.service.AIProviderConfigService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AI Provider Config Service (Read-Only)
 * Configurations are loaded from application.yml at startup
 */
@Service
public class AIProviderConfigServiceImpl implements AIProviderConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIProviderConfigServiceImpl.class);
    
    @Autowired
    private AIProviderConfigRepository repository;
    
    // DashScope configuration
    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String dashscopeModel;
    
    // Ollama configuration
    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${spring.ai.ollama.chat.options.model:llama2}")
    private String ollamaModel;
    
    // OpenAI configuration
    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String openaiBaseUrl;
    
    /**
     * Initialize providers from application.yml at startup
     */
    @PostConstruct
    public void init() {
        initializeProvidersFromConfig();
    }
    
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
    public AIProviderConfigDTO updateProviderModels(String id, AIProviderConfigDTO providerDTO) {
        AIProviderConfig existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        
        // Only update models and defaultModel
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
        
        if (providerDTO.getDefaultModel() != null) {
            existing.setDefaultModel(providerDTO.getDefaultModel());
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUserId());
        
        AIProviderConfig saved = repository.save(existing);
        logger.info("Updated models for AI provider: {}", saved.getProvider());
        
        return convertToDTO(saved);
    }
    
    @Override
    public void initializeProvidersFromConfig() {
        logger.info("Initializing AI provider configurations from application.yml...");
        
        // DashScope - Read from application.yml
        if (!repository.existsByProvider("dashscope")) {
            AIProviderConfig dashscope = new AIProviderConfig();
            dashscope.setProvider("dashscope");
            dashscope.setDisplayName("DashScope (百炼)");
            dashscope.setDescription("阿里云通义千问大模型");
            dashscope.setEnabled(true);
            dashscope.setBaseUrl("https://dashscope.aliyuncs.com/api/v1");  // Fixed URL from config
            dashscope.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("qwen3-code-plus", "通义千问代码-Plus", "代码生成专用模型", true),
                    new AIProviderConfig.ModelConfig("qwen3-max", "通义千问-Max", "最强性能模型", true),
                    new AIProviderConfig.ModelConfig("qwen3-plus", "通义千问-Plus", "高性价比模型", true)
            ));
            dashscope.setDefaultModel(dashscopeModel);  // From application.yml
            dashscope.setCreatedAt(LocalDateTime.now());
            dashscope.setUpdatedAt(LocalDateTime.now());
            dashscope.setCreatedBy("system");
            dashscope.setUpdatedBy("system");
            repository.save(dashscope);
            logger.info("Initialized DashScope provider with base URL from application.yml");
        } else {
            // Update existing provider with config values
            AIProviderConfig existing = repository.findByProvider("dashscope").orElseThrow();
            existing.setBaseUrl("https://dashscope.aliyuncs.com/api/v1");
            existing.setDefaultModel(dashscopeModel);
            existing.setUpdatedAt(LocalDateTime.now());
            repository.save(existing);
            logger.info("Updated DashScope provider from application.yml");
        }
        
        // Ollama - Read from application.yml
        if (!repository.existsByProvider("ollama")) {
            AIProviderConfig ollama = new AIProviderConfig();
            ollama.setProvider("ollama");
            ollama.setDisplayName("Ollama");
            ollama.setDescription("本地运行的开源大模型");
            ollama.setEnabled(true);
            ollama.setBaseUrl(ollamaBaseUrl);  // From application.yml
            ollama.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("qwen3:8b", "Qwen 3 8B", "阿里云百炼大模型", true),
                    new AIProviderConfig.ModelConfig("qwen3:32b", "Qwen 3 32B", "高性能开源模型", true),
                    new AIProviderConfig.ModelConfig("llama2", "Llama 2", "Meta开源模型", true)
            ));
            ollama.setDefaultModel(ollamaModel);  // From application.yml
            ollama.setCreatedAt(LocalDateTime.now());
            ollama.setUpdatedAt(LocalDateTime.now());
            ollama.setCreatedBy("system");
            ollama.setUpdatedBy("system");
            repository.save(ollama);
            logger.info("Initialized Ollama provider with base URL: {}", ollamaBaseUrl);
        } else {
            // Update existing provider with config values
            AIProviderConfig existing = repository.findByProvider("ollama").orElseThrow();
            existing.setBaseUrl(ollamaBaseUrl);
            existing.setDefaultModel(ollamaModel);
            existing.setUpdatedAt(LocalDateTime.now());
            repository.save(existing);
            logger.info("Updated Ollama provider from application.yml");
        }
        
        // OpenAI - Read from application.yml
        if (!repository.existsByProvider("openai")) {
            AIProviderConfig openai = new AIProviderConfig();
            openai.setProvider("openai");
            openai.setDisplayName("OpenAI");
            openai.setDescription("OpenAI GPT模型");
            openai.setEnabled(true);
            openai.setBaseUrl(openaiBaseUrl);  // From application.yml
            openai.setModels(Arrays.asList(
                    new AIProviderConfig.ModelConfig("gpt-3.5-turbo", "GPT-3.5 Turbo", "快速且经济的模型", true),
                    new AIProviderConfig.ModelConfig("gpt-4", "GPT-4", "最强性能模型", true),
                    new AIProviderConfig.ModelConfig("gpt-4-turbo", "GPT-4 Turbo", "更快的GPT-4", true)
            ));
            openai.setDefaultModel("gpt-3.5-turbo");
            openai.setCreatedAt(LocalDateTime.now());
            openai.setUpdatedAt(LocalDateTime.now());
            openai.setCreatedBy("system");
            openai.setUpdatedBy("system");
            repository.save(openai);
            logger.info("Initialized OpenAI provider with base URL: {}", openaiBaseUrl);
        } else {
            // Update existing provider with config values
            AIProviderConfig existing = repository.findByProvider("openai").orElseThrow();
            existing.setBaseUrl(openaiBaseUrl);
            existing.setUpdatedAt(LocalDateTime.now());
            repository.save(existing);
            logger.info("Updated OpenAI provider from application.yml");
        }
        
        logger.info("AI provider configurations initialized from application.yml");
    }
    
    /**
     * Convert entity to DTO (no API key exposed)
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
        
        // Don't expose API key - it's configured in application.yml
        // API key field has been removed from DTO
        
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
