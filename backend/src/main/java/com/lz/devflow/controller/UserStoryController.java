package com.lz.devflow.controller;

import com.lz.devflow.dto.CreateUserStoryRequest;
import com.lz.devflow.dto.UserStoryResponse;
import com.lz.devflow.service.UserStoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for User Story operations
 */
@RestController
@RequestMapping("/api/user-stories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserStoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserStoryController.class);
    
    private final UserStoryService userStoryService;
    
    public UserStoryController(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }
    
    /**
     * Create a new user story
     */
    @PostMapping
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> createUserStory(@Valid @RequestBody CreateUserStoryRequest request) {
        logger.info("Creating user story: {}", request.getTitle());
        
        try {
            String currentUserId = getCurrentUserId();
            UserStoryResponse response = userStoryService.createUserStory(request, currentUserId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User story created successfully",
                "data", response
            ));
        } catch (Exception e) {
            logger.error("Error creating user story", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to create user story: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user story by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> getUserStoryById(@PathVariable String id) {
        logger.info("Fetching user story: {}", id);
        
        try {
            return userStoryService.getUserStoryById(id)
                    .map(story -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", story
                    )))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User story not found"
                    )));
        } catch (Exception e) {
            logger.error("Error fetching user story", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch user story: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all user stories
     */
    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> getAllUserStories(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String status) {
        
        logger.info("Fetching user stories - projectId: {}, ownerId: {}, status: {}", projectId, ownerId, status);
        
        try {
            List<UserStoryResponse> stories;
            
            if (projectId != null && !projectId.isEmpty()) {
                stories = userStoryService.getUserStoriesByProjectId(projectId);
            } else if (ownerId != null && !ownerId.isEmpty()) {
                stories = userStoryService.getUserStoriesByOwnerId(ownerId);
            } else if (status != null && !status.isEmpty()) {
                stories = userStoryService.getUserStoriesByStatus(status);
            } else {
                stories = userStoryService.getAllUserStories();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stories,
                "total", stories.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching user stories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch user stories: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update user story
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> updateUserStory(
            @PathVariable String id,
            @Valid @RequestBody CreateUserStoryRequest request) {
        
        logger.info("Updating user story: {}", id);
        
        try {
            String currentUserId = getCurrentUserId();
            UserStoryResponse response = userStoryService.updateUserStory(id, request, currentUserId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User story updated successfully",
                "data", response
            ));
        } catch (RuntimeException e) {
            logger.error("User story not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error updating user story", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to update user story: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update user story status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> updateUserStoryStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        
        String status = payload.get("status");
        logger.info("Updating user story {} status to {}", id, status);
        
        try {
            String currentUserId = getCurrentUserId();
            UserStoryResponse response = userStoryService.updateUserStoryStatus(id, status, currentUserId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User story status updated successfully",
                "data", response
            ));
        } catch (RuntimeException e) {
            logger.error("User story not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error updating user story status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to update user story status: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete user story
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> deleteUserStory(@PathVariable String id) {
        logger.info("Deleting user story: {}", id);
        
        try {
            userStoryService.deleteUserStory(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User story deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Error deleting user story", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to delete user story: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Download batch import template
     */
    @GetMapping("/batch-import/template")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> downloadBatchImportTemplate() {
        logger.info("Downloading batch import template");
        
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("storyTemplate.xlsx");
            
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Template file not found"
                ));
            }
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user_story_template.xlsx\"")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to download template: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Batch import user stories
     */
    @PostMapping("/batch-import")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> batchImportUserStories(@RequestBody Map<String, List<CreateUserStoryRequest>> payload) {
        logger.info("Batch importing user stories");
        
        try {
            List<CreateUserStoryRequest> userStories = payload.get("userStories");
            
            if (userStories == null || userStories.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "No user stories provided"
                ));
            }
            
            String currentUserId = getCurrentUserId();
            List<UserStoryResponse> responses = userStoryService.batchCreateUserStories(userStories, currentUserId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", String.format("Successfully imported %d user stories", responses.size()),
                "data", responses
            ));
        } catch (Exception e) {
            logger.error("Error batch importing user stories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to batch import user stories: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get current authenticated user's ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
