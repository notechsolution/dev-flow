package com.lz.devflow.dto;

import com.lz.devflow.constant.PromptLevel;
import com.lz.devflow.constant.PromptType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating/updating prompt template
 */
public class PromptTemplateRequest {
    
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    @NotNull(message = "Type cannot be null")
    private PromptType type;
    
    @NotNull(message = "Level cannot be null")
    private PromptLevel level;
    
    @NotBlank(message = "Content cannot be blank")
    private String content;
    
    private String description;
    
    private String projectId;  // Required if level is PROJECT
    
    private String userId;     // Required if level is USER
    
    private Boolean enabled;

    // Constructors
    public PromptTemplateRequest() {
    }

    // Getters and Setters
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
