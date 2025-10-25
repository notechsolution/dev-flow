package com.lz.devflow.dto;

/**
 * Response DTO for user story optimization
 */
public class UserStoryOptimizationResponse {
    
    private String optimizedDescription;
    
    private boolean success;
    
    private String message;

    public UserStoryOptimizationResponse() {}

    public UserStoryOptimizationResponse(String optimizedDescription, boolean success, String message) {
        this.optimizedDescription = optimizedDescription;
        this.success = success;
        this.message = message;
    }

    public static UserStoryOptimizationResponse success(String optimizedDescription) {
        return new UserStoryOptimizationResponse(optimizedDescription, true, "Success");
    }

    public static UserStoryOptimizationResponse error(String message) {
        return new UserStoryOptimizationResponse(null, false, message);
    }

    public String getOptimizedDescription() {
        return optimizedDescription;
    }

    public void setOptimizedDescription(String optimizedDescription) {
        this.optimizedDescription = optimizedDescription;
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