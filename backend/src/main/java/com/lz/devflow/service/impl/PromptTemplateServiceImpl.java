package com.lz.devflow.service.impl;

import com.lz.devflow.constant.PromptLevel;
import com.lz.devflow.constant.PromptType;
import com.lz.devflow.dto.PromptTemplateRequest;
import com.lz.devflow.dto.PromptTemplateResponse;
import com.lz.devflow.entity.PromptTemplate;
import com.lz.devflow.repository.PromptTemplateRepository;
import com.lz.devflow.service.PromptTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of PromptTemplateService
 */
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateServiceImpl.class);
    
    private final PromptTemplateRepository promptTemplateRepository;
    
    @Value("classpath:prompts/requirement-clarification.st")
    private Resource clarificationPromptResource;
    
    @Value("classpath:prompts/requirement-optimization.st")
    private Resource optimizationPromptResource;

    public PromptTemplateServiceImpl(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Override
    public PromptTemplateResponse getEffectivePromptTemplate(PromptType type, String userId, String projectId) {
        logger.debug("Getting effective prompt template for type: {}, userId: {}, projectId: {}", 
                     type, userId, projectId);
        
        // 优先级1: 用户级别
        if (userId != null) {
            List<PromptTemplate> userTemplates = promptTemplateRepository
                .findByTypeAndLevelAndUserIdAndEnabled(type, PromptLevel.USER, userId, true);
            if (!userTemplates.isEmpty()) {
                logger.debug("Found user-level template");
                return PromptTemplateResponse.fromEntity(userTemplates.get(0));
            }
        }
        
        // 优先级2: 项目级别
        if (projectId != null) {
            List<PromptTemplate> projectTemplates = promptTemplateRepository
                .findByTypeAndLevelAndProjectIdAndEnabled(type, PromptLevel.PROJECT, projectId, true);
            if (!projectTemplates.isEmpty()) {
                logger.debug("Found project-level template");
                return PromptTemplateResponse.fromEntity(projectTemplates.get(0));
            }
        }
        
        // 优先级3: 系统级别（默认）
        logger.debug("Using system-level default template");
        return getSystemDefaultTemplate(type);
    }

    @Override
    public PromptTemplateResponse getSystemDefaultTemplate(PromptType type) {
        Optional<PromptTemplate> defaultTemplate = promptTemplateRepository
            .findByTypeAndLevelAndIsDefaultAndEnabled(type, PromptLevel.SYSTEM, true, true);
        
        if (defaultTemplate.isPresent()) {
            return PromptTemplateResponse.fromEntity(defaultTemplate.get());
        }
        
        // If no default template found in DB, return null (should initialize first)
        logger.warn("No system default template found for type: {}", type);
        return null;
    }

    @Override
    public List<PromptTemplateResponse> getProjectTemplates(String projectId, PromptType type) {
        List<PromptTemplate> templates;
        
        if (type != null) {
            templates = promptTemplateRepository
                .findByTypeAndLevelAndProjectId(type, PromptLevel.PROJECT, projectId);
        } else {
            templates = promptTemplateRepository.findByProjectId(projectId);
        }
        
        return templates.stream()
            .map(PromptTemplateResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplateResponse> getUserTemplates(String userId, PromptType type) {
        List<PromptTemplate> templates;
        
        if (type != null) {
            templates = promptTemplateRepository
                .findByTypeAndLevelAndUserId(type, PromptLevel.USER, userId);
        } else {
            templates = promptTemplateRepository.findByUserId(userId);
        }
        
        return templates.stream()
            .map(PromptTemplateResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplateResponse> getSystemTemplates(PromptType type) {
        List<PromptTemplate> templates;
        
        if (type != null) {
            templates = promptTemplateRepository.findByTypeAndLevel(type, PromptLevel.SYSTEM);
        } else {
            templates = promptTemplateRepository.findByLevel(PromptLevel.SYSTEM);
        }
        
        return templates.stream()
            .map(PromptTemplateResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public PromptTemplateResponse createTemplate(PromptTemplateRequest request, String currentUserId) {
        logger.info("Creating prompt template: {}, level: {}", request.getName(), request.getLevel());
        
        // Validate request
        validateRequest(request);
        
        // Check for duplicate name
        if (isDuplicateName(request)) {
            throw new IllegalArgumentException("A template with this name already exists at this level");
        }
        
        // Create entity
        PromptTemplate template = new PromptTemplate();
        template.setName(request.getName());
        template.setType(request.getType());
        template.setLevel(request.getLevel());
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        template.setProjectId(request.getProjectId());
        template.setUserId(request.getUserId());
        template.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        template.setIsDefault(false); // User/Project templates are never default
        template.setCreatedBy(currentUserId);
        template.setUpdatedBy(currentUserId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        PromptTemplate saved = promptTemplateRepository.save(template);
        logger.info("Prompt template created successfully with ID: {}", saved.getId());
        
        return PromptTemplateResponse.fromEntity(saved);
    }

    @Override
    public PromptTemplateResponse updateTemplate(String id, PromptTemplateRequest request, String currentUserId) {
        logger.info("Updating prompt template: {}", id);
        
        PromptTemplate existing = promptTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));
        
        // Don't allow updating system default templates
        if (existing.getIsDefault() && existing.getLevel() == PromptLevel.SYSTEM) {
            throw new IllegalArgumentException("Cannot update system default templates");
        }
        
        // Validate request
        validateRequest(request);
        
        // Update fields
        existing.setName(request.getName());
        existing.setContent(request.getContent());
        existing.setDescription(request.getDescription());
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        existing.setUpdatedBy(currentUserId);
        existing.setUpdatedAt(LocalDateTime.now());
        
        PromptTemplate updated = promptTemplateRepository.save(existing);
        logger.info("Prompt template updated successfully");
        
        return PromptTemplateResponse.fromEntity(updated);
    }

    @Override
    public void deleteTemplate(String id, String currentUserId) {
        logger.info("Deleting prompt template: {}", id);
        
        PromptTemplate template = promptTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));
        
        // Don't allow deleting system default templates
        if (template.getIsDefault() && template.getLevel() == PromptLevel.SYSTEM) {
            throw new IllegalArgumentException("Cannot delete system default templates");
        }
        
        promptTemplateRepository.delete(template);
        logger.info("Prompt template deleted successfully");
    }

    @Override
    public PromptTemplateResponse getTemplateById(String id) {
        PromptTemplate template = promptTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));
        
        return PromptTemplateResponse.fromEntity(template);
    }

    @Override
    public void initializeSystemTemplates() {
        logger.info("Initializing system default prompt templates");
        
        try {
            // Initialize clarification template
            initializeTemplate(
                PromptType.REQUIREMENT_CLARIFICATION,
                "系统默认需求澄清模板",
                "系统提供的默认需求澄清提示词模板",
                clarificationPromptResource
            );
            
            // Initialize optimization template
            initializeTemplate(
                PromptType.REQUIREMENT_OPTIMIZATION,
                "系统默认需求优化模板",
                "系统提供的默认需求优化提示词模板",
                optimizationPromptResource
            );
            
            logger.info("System default prompt templates initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize system default prompt templates", e);
            throw new RuntimeException("Failed to initialize system templates", e);
        }
    }
    
    /**
     * Initialize a single template
     */
    private void initializeTemplate(PromptType type, String name, String description, Resource resource) {
        // Check if default template already exists
        Optional<PromptTemplate> existing = promptTemplateRepository
            .findByTypeAndLevelAndIsDefaultAndEnabled(type, PromptLevel.SYSTEM, true, true);
        
        if (existing.isPresent()) {
            logger.debug("System default template for type {} already exists", type);
            return;
        }
        
        try {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            
            PromptTemplate template = new PromptTemplate();
            template.setName(name);
            template.setType(type);
            template.setLevel(PromptLevel.SYSTEM);
            template.setContent(content);
            template.setDescription(description);
            template.setIsDefault(true);
            template.setEnabled(true);
            template.setCreatedBy("SYSTEM");
            template.setUpdatedBy("SYSTEM");
            
            promptTemplateRepository.save(template);
            logger.info("Initialized system default template for type: {}", type);
            
        } catch (Exception e) {
            logger.error("Failed to initialize template for type: {}", type, e);
            throw new RuntimeException("Failed to initialize template", e);
        }
    }
    
    /**
     * Validate template request
     */
    private void validateRequest(PromptTemplateRequest request) {
        if (request.getLevel() == PromptLevel.PROJECT && request.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required for project-level templates");
        }
        
        if (request.getLevel() == PromptLevel.USER && request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for user-level templates");
        }
        
        if (request.getLevel() == PromptLevel.SYSTEM) {
            throw new IllegalArgumentException("Cannot create system-level templates via API");
        }
    }
    
    /**
     * Check for duplicate template name
     */
    private boolean isDuplicateName(PromptTemplateRequest request) {
        if (request.getLevel() == PromptLevel.USER) {
            return promptTemplateRepository.existsByTypeAndLevelAndUserIdAndName(
                request.getType(), request.getLevel(), request.getUserId(), request.getName());
        } else if (request.getLevel() == PromptLevel.PROJECT) {
            return promptTemplateRepository.existsByTypeAndLevelAndProjectIdAndName(
                request.getType(), request.getLevel(), request.getProjectId(), request.getName());
        }
        return false;
    }
}
