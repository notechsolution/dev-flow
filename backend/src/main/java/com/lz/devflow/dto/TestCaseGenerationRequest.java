package com.lz.devflow.dto;

/**
 * Request DTO for test case generation
 */
public class TestCaseGenerationRequest {
    
    private String description;
    
    private String optimizedDescription;
    
    private String projectContext;

    public TestCaseGenerationRequest() {}

    public TestCaseGenerationRequest(String description, String optimizedDescription, String projectContext) {
        this.description = description;
        this.optimizedDescription = optimizedDescription;
        this.projectContext = projectContext;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOptimizedDescription() {
        return optimizedDescription;
    }

    public void setOptimizedDescription(String optimizedDescription) {
        this.optimizedDescription = optimizedDescription;
    }

    public String getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(String projectContext) {
        this.projectContext = projectContext;
    }
}