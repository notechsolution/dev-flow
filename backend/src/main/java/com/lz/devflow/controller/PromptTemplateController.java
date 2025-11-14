package com.lz.devflow.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lz.devflow.constant.PromptType;
import com.lz.devflow.dto.PromptTemplateRequest;
import com.lz.devflow.dto.PromptTemplateResponse;
import com.lz.devflow.service.PromptTemplateService;
import com.lz.devflow.util.SecurityUtils;

import jakarta.validation.Valid;

/**
 * REST Controller for Prompt Template management
 */
@RestController
@RequestMapping("/api/prompt-templates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PromptTemplateController {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateController.class);
    
    private final PromptTemplateService promptTemplateService;

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * Get effective prompt template (user > project > system)
     */
    @GetMapping("/effective")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<PromptTemplateResponse> getEffectiveTemplate(
            @RequestParam PromptType type,
            @RequestParam(required = false) String projectId) {
        
        logger.info("Getting effective prompt template for type: {}, projectId: {}", type, projectId);
        
        try {
            String userId = SecurityUtils.getCurrentUserId();
            PromptTemplateResponse template = promptTemplateService
                .getEffectivePromptTemplate(type, userId, projectId);
            
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            logger.error("Error getting effective prompt template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get system default template
     */
    @GetMapping("/system/default")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<PromptTemplateResponse> getSystemDefaultTemplate(
            @RequestParam PromptType type) {
        
        logger.info("Getting system default template for type: {}", type);
        
        try {
            PromptTemplateResponse template = promptTemplateService.getSystemDefaultTemplate(type);
            
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            logger.error("Error getting system default template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all system templates
     */
    @GetMapping("/system")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<List<PromptTemplateResponse>> getSystemTemplates(
            @RequestParam(required = false) PromptType type) {
        
        logger.info("Getting system templates, type: {}", type);
        
        try {
            List<PromptTemplateResponse> templates = promptTemplateService.getSystemTemplates(type);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error getting system templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get project-level templates
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<List<PromptTemplateResponse>> getProjectTemplates(
            @PathVariable String projectId,
            @RequestParam(required = false) PromptType type) {
        
        logger.info("Getting project templates for projectId: {}, type: {}", projectId, type);
        
        try {
            List<PromptTemplateResponse> templates = promptTemplateService
                .getProjectTemplates(projectId, type);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error getting project templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user-level templates
     */
    @GetMapping("/user/my-templates")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<List<PromptTemplateResponse>> getMyTemplates(
            @RequestParam(required = false) PromptType type) {
        
        logger.info("Getting user templates for current user, type: {}", type);
        
        try {
            String userId = SecurityUtils.getCurrentUserId();
            List<PromptTemplateResponse> templates = promptTemplateService
                .getUserTemplates(userId, type);
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error getting user templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<PromptTemplateResponse> getTemplateById(@PathVariable String id) {
        
        logger.info("Getting template by ID: {}", id);
        
        try {
            PromptTemplateResponse template = promptTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Template not found: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error getting template by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new template (user or project level)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<PromptTemplateResponse> createTemplate(
            @Valid @RequestBody PromptTemplateRequest request) {
        
        logger.info("Creating new prompt template: {}", request.getName());
        
        try {
            String userId =  SecurityUtils.getCurrentUserId();
            PromptTemplateResponse created = promptTemplateService.createTemplate(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid template creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<PromptTemplateResponse> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody PromptTemplateRequest request) {
        
        logger.info("Updating prompt template: {}", id);
        
        try {
            String userId =  SecurityUtils.getCurrentUserId();
            request.setUserId(userId);
            PromptTemplateResponse updated = promptTemplateService.updateTemplate(id, request, userId);
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid template update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error updating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a template
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        
        logger.info("Deleting prompt template: {}", id);
        
        try {
            String userId = SecurityUtils.getCurrentUserId();
            promptTemplateService.deleteTemplate(id, userId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid template deletion request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error deleting template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Initialize system default templates (Admin only)
     */
    @PostMapping("/system/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> initializeSystemTemplates() {
        
        logger.info("Initializing system default templates");
        
        try {
            promptTemplateService.initializeSystemTemplates();
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error initializing system templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
