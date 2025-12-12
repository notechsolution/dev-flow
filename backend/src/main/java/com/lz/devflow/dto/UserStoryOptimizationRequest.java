package com.lz.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user story optimization
 */
public class UserStoryOptimizationRequest {
    
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    private String projectContext;
    
    private String additionalRequirements;

    private String projectId;
    
    // AI provider to use (dashscope, ollama, openai). If not specified, uses default provider
    private String provider;

    public UserStoryOptimizationRequest() {}

    public UserStoryOptimizationRequest(String description, String projectContext, String additionalRequirements) {
        this.description = description;
        this.projectContext = projectContext;
        this.additionalRequirements = additionalRequirements;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(String projectContext) {
        this.projectContext = projectContext;
    }

    public String getAdditionalRequirements() {
        return additionalRequirements;
    }

    public void setAdditionalRequirements(String additionalRequirements) {
        this.additionalRequirements = additionalRequirements;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
}