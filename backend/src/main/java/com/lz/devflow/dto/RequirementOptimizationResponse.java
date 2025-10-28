package com.lz.devflow.dto;

/**
 * Response DTO for final requirement optimization
 */
public class RequirementOptimizationResponse {
    
    private String optimizedRequirement;
    private String userStory;
    private String acceptanceCriteria;
    private String technicalNotes;
    private boolean success;
    private String message;

    // Constructors
    public RequirementOptimizationResponse() {
    }

    public RequirementOptimizationResponse(String optimizedRequirement, String userStory, 
                                          String acceptanceCriteria, String technicalNotes,
                                          boolean success, String message) {
        this.optimizedRequirement = optimizedRequirement;
        this.userStory = userStory;
        this.acceptanceCriteria = acceptanceCriteria;
        this.technicalNotes = technicalNotes;
        this.success = success;
        this.message = message;
    }

    // Static factory methods
    public static RequirementOptimizationResponse success(String optimizedRequirement, String userStory,
                                                         String acceptanceCriteria, String technicalNotes) {
        return new RequirementOptimizationResponse(optimizedRequirement, userStory, 
                                                   acceptanceCriteria, technicalNotes,
                                                   true, "Requirement optimized successfully");
    }

    public static RequirementOptimizationResponse error(String message) {
        return new RequirementOptimizationResponse("", "", "", "", false, message);
    }

    // Getters and Setters
    public String getOptimizedRequirement() {
        return optimizedRequirement;
    }

    public void setOptimizedRequirement(String optimizedRequirement) {
        this.optimizedRequirement = optimizedRequirement;
    }

    public String getUserStory() {
        return userStory;
    }

    public void setUserStory(String userStory) {
        this.userStory = userStory;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getTechnicalNotes() {
        return technicalNotes;
    }

    public void setTechnicalNotes(String technicalNotes) {
        this.technicalNotes = technicalNotes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
