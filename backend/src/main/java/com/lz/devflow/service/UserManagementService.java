package com.lz.devflow.service;

import com.lz.devflow.dto.CreateUserRequest;
import com.lz.devflow.dto.UpdateUserRequest;
import com.lz.devflow.dto.UserResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations
 */
public interface UserManagementService {
    
    /**
     * Create a new user
     */
    UserResponse createUser(CreateUserRequest request, String currentUserId);
    
    /**
     * Get user by ID
     */
    Optional<UserResponse> getUserById(String id);
    
    /**
     * Get all users
     */
    List<UserResponse> getAllUsers();
    
    /**
     * Get users by role
     */
    List<UserResponse> getUsersByRole(String role);
    
    /**
     * Get users by project ID
     */
    List<UserResponse> getUsersByProjectId(String projectId);
    
    /**
     * Update user
     */
    UserResponse updateUser(String id, UpdateUserRequest request, String currentUserId);
    
    /**
     * Delete user
     */
    void deleteUser(String id);
    
    /**
     * Check if user has access to project
     */
    boolean hasProjectAccess(String userId, String projectId);
    
    /**
     * Check if user is admin of project
     */
    boolean isProjectAdmin(String userId, String projectId);
    
    /**
     * Check if user is operator
     */
    boolean isOperator(String userId);
}
