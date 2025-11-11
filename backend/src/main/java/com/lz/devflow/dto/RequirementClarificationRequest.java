package com.lz.devflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for requirement clarification
 */
public class RequirementClarificationRequest {
    
    @NotBlank(message = "Original requirement cannot be blank")
    private String originalRequirement;
    
    private String title;
    
    // Business context to help AI better understand the requirement
    private String projectContext;

    // Constructors
    public RequirementClarificationRequest() {
    }

    public RequirementClarificationRequest(String originalRequirement, String title, String projectContext) {
        this.originalRequirement = originalRequirement;
        this.title = title;
        this.projectContext = projectContext;
    }

    // Getters and Setters
    public String getOriginalRequirement() {
        return originalRequirement;
    }

    public void setOriginalRequirement(String originalRequirement) {
        this.originalRequirement = originalRequirement;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(String projectContext) {
        this.projectContext = projectContext;
    }
}
