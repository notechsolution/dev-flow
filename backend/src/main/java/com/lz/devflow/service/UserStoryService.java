package com.lz.devflow.service;

import com.lz.devflow.dto.CreateUserStoryRequest;
import com.lz.devflow.dto.UserStoryResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User Story operations
 */
public interface UserStoryService {
    
    /**
     * Create a new user story
     */
    UserStoryResponse createUserStory(CreateUserStoryRequest request, String currentUserId);
    
    /**
     * Get user story by ID
     */
    Optional<UserStoryResponse> getUserStoryById(String id);
    
    /**
     * Get all user stories
     */
    List<UserStoryResponse> getAllUserStories();
    
    /**
     * Get user stories by project ID
     */
    List<UserStoryResponse> getUserStoriesByProjectId(String projectId);
    
    /**
     * Get user stories by owner ID
     */
    List<UserStoryResponse> getUserStoriesByOwnerId(String ownerId);
    
    /**
     * Get user stories by status
     */
    List<UserStoryResponse> getUserStoriesByStatus(String status);
    
    /**
     * Update user story
     */
    UserStoryResponse updateUserStory(String id, CreateUserStoryRequest request, String currentUserId);
    
    /**
     * Delete user story
     */
    void deleteUserStory(String id);
    
    /**
     * Update user story status
     */
    UserStoryResponse updateUserStoryStatus(String id, String status, String currentUserId);
    
    /**
     * Batch create user stories without AI optimization or Zentao sync
     */
    List<UserStoryResponse> batchCreateUserStories(List<CreateUserStoryRequest> requests, String currentUserId);
}
