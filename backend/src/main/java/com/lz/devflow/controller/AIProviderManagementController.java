package com.lz.devflow.controller;

import com.lz.devflow.dto.AIProviderConfigDTO;
import com.lz.devflow.service.AIProviderConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI Provider Configuration Management
 * Only accessible by OPERATOR role
 */
@RestController
@RequestMapping("/api/admin/ai-providers")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('OPERATOR')")
public class AIProviderManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AIProviderManagementController.class);
    
    @Resource
    private AIProviderConfigService providerConfigService;
    
    /**
     * Get all AI provider configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProviders() {
        logger.info("Getting all AI provider configurations");
        
        try {
            List<AIProviderConfigDTO> providers = providerConfigService.getAllProviders();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", providers,
                    "message", "获取AI提供商配置成功"
            ));
        } catch (Exception e) {
            logger.error("Error getting AI providers", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取AI提供商配置失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get specific provider configuration
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProvider(@PathVariable String id) {
        logger.info("Getting AI provider configuration: {}", id);
        
        try {
            AIProviderConfigDTO provider = providerConfigService.getProviderById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", provider,
                    "message", "获取AI提供商配置成功"
            ));
        } catch (Exception e) {
            logger.error("Error getting AI provider: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Create new provider configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProvider(
            @Valid @RequestBody AIProviderConfigDTO providerDTO) {
        logger.info("Creating AI provider configuration: {}", providerDTO.getProvider());
        
        try {
            AIProviderConfigDTO created = providerConfigService.createProvider(providerDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "创建AI提供商配置成功"
            ));
        } catch (Exception e) {
            logger.error("Error creating AI provider", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Update provider configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProvider(
            @PathVariable String id,
            @Valid @RequestBody AIProviderConfigDTO providerDTO) {
        logger.info("Updating AI provider configuration: {}", id);
        
        try {
            AIProviderConfigDTO updated = providerConfigService.updateProvider(id, providerDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "更新AI提供商配置成功"
            ));
        } catch (Exception e) {
            logger.error("Error updating AI provider: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Toggle provider enabled/disabled
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleProvider(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        logger.info("Toggling AI provider: {}", id);
        
        try {
            boolean enabled = request.getOrDefault("enabled", false);
            AIProviderConfigDTO updated = providerConfigService.toggleProvider(id, enabled);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", enabled ? "AI提供商已启用" : "AI提供商已禁用"
            ));
        } catch (Exception e) {
            logger.error("Error toggling AI provider: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Delete provider configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProvider(@PathVariable String id) {
        logger.info("Deleting AI provider configuration: {}", id);
        
        try {
            providerConfigService.deleteProvider(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "删除AI提供商配置成功"
            ));
        } catch (Exception e) {
            logger.error("Error deleting AI provider: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Initialize default provider configurations
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeDefaultProviders() {
        logger.info("Initializing default AI provider configurations");
        
        try {
            providerConfigService.initializeDefaultProviders();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "默认AI提供商配置初始化成功"
            ));
        } catch (Exception e) {
            logger.error("Error initializing default providers", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "初始化失败: " + e.getMessage()
            ));
        }
    }
}
