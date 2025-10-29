package com.lz.devflow.controller;

import com.lz.devflow.dto.CreateProjectRequest;
import com.lz.devflow.dto.ProjectResponse;
import com.lz.devflow.dto.UpdateProjectRequest;
import com.lz.devflow.service.ProjectService;
import com.lz.devflow.util.SecurityUtils;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Project Management
 * Handles CRUD operations with role-based access control
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * Create a new project
     * Only OPERATOR can create projects
     */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserName();
            ProjectResponse project = projectService.createProject(request, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", project);
            response.put("message", "Project created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create project: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get all projects accessible by current user
     * OPERATOR sees all projects
     * ADMIN and USER see only assigned projects
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'USER')")
    public ResponseEntity<Map<String, Object>> getAllProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserName();
            List<ProjectResponse> projects = projectService.getAllProjects(currentUserId, name, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", projects);
            response.put("total", projects.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch projects: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get project by ID
     * User must have access to the project
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'USER')")
    public ResponseEntity<Map<String, Object>> getProjectById(@PathVariable String id) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserName();
            ProjectResponse project = projectService.getProjectById(id, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", project);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch project: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update project
     * Only OPERATOR or project ADMIN can modify
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProject(
            @PathVariable String id,
            @RequestBody UpdateProjectRequest request) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserName();
            ProjectResponse project = projectService.updateProject(id, request, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", project);
            response.put("message", "Project updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update project: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete project
     * Only OPERATOR can delete projects
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> deleteProject(@PathVariable String id) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserName();
            projectService.deleteProject(id, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Project deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete project: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    

}
