package com.lz.devflow.service;

import com.lz.devflow.dto.CreateProjectRequest;
import com.lz.devflow.dto.ProjectResponse;
import com.lz.devflow.dto.UpdateProjectRequest;
import com.lz.devflow.entity.Project;

import java.util.List;

public interface ProjectService {
    
    /**
     * Create a new project (OPERATOR only)
     */
    ProjectResponse createProject(CreateProjectRequest request, String currentUserId);
    
    /**
     * Get project by ID
     */
    ProjectResponse getProjectById(String projectId, String currentUserId);
    
    /**
     * Get all projects accessible by current user
     */
    List<ProjectResponse> getAllProjects(String currentUserId, String nameFilter, String statusFilter);
    
    /**
     * Update project (OPERATOR or project ADMIN)
     */
    ProjectResponse updateProject(String projectId, UpdateProjectRequest request, String currentUserId);
    
    /**
     * Delete project (OPERATOR only)
     */
    void deleteProject(String projectId, String currentUserId);
    
    /**
     * Check if user can access project
     */
    boolean canAccessProject(String projectId, String userId);
    
    /**
     * Check if user can modify project (OPERATOR or project ADMIN)
     */
    boolean canModifyProject(String projectId, String userId);
    
    /**
     * Convert entity to response DTO (masks access tokens)
     */
    ProjectResponse toProjectResponse(Project project);
}
