package com.lz.devflow.service.pms;

import com.lz.devflow.entity.UserStory;

/**
 * Project Management System Service Interface
 * This interface defines the contract for integrating with different project management systems
 */
public interface ProjectManagementSystemService {
    
    /**
     * Create a story/requirement in the project management system
     * 
     * @param userStory The user story to create
     * @param systemId The project/product ID in the PM system
     * @param baseUrl The base URL of the PM system
     * @param accessToken The access token for authentication
     * @return The story ID from the PM system
     */
    String createStory(UserStory userStory, String systemId, String baseUrl, String accessToken);
    
    /**
     * Get the type of project management system
     * 
     * @return The type identifier (e.g., "Zentao", "Jira", "Azure DevOps")
     */
    String getType();
}
