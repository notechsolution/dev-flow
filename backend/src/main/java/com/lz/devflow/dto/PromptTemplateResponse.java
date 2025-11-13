package com.lz.devflow.dto;

import com.lz.devflow.constant.PromptLevel;
import com.lz.devflow.constant.PromptType;
import com.lz.devflow.entity.PromptTemplate;

import java.time.LocalDateTime;

/**
 * Response DTO for prompt template
 */
public class PromptTemplateResponse {
    
    private String id;
    private String name;
    private PromptType type;
    private PromptLevel level;
    private String content;
    private String description;
    private String projectId;
    private String userId;
    private Boolean isDefault;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public PromptTemplateResponse() {
    }

    // Static factory method
    public static PromptTemplateResponse fromEntity(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        
        PromptTemplateResponse response = new PromptTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setType(template.getType());
        response.setLevel(template.getLevel());
        response.setContent(template.getContent());
        response.setDescription(template.getDescription());
        response.setProjectId(template.getProjectId());
        response.setUserId(template.getUserId());
        response.setIsDefault(template.getIsDefault());
        response.setEnabled(template.getEnabled());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setCreatedBy(template.getCreatedBy());
        response.setUpdatedBy(template.getUpdatedBy());
        
        return response;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PromptType getType() {
        return type;
    }

    public void setType(PromptType type) {
        this.type = type;
    }

    public PromptLevel getLevel() {
        return level;
    }

    public void setLevel(PromptLevel level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
