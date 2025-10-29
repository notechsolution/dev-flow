package com.lz.devflow.controller;

import com.lz.devflow.dto.CreateUserRequest;
import com.lz.devflow.dto.UpdateUserRequest;
import com.lz.devflow.dto.UserResponse;
import com.lz.devflow.service.UserManagementService;
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
 * REST Controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    private final UserManagementService userManagementService;
    
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }
    
    /**
     * Create a new user (Only OPERATOR and ADMIN can create users)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).OPERATOR.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name())")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating user: {}", request.getUsername());
        
        try {
            String currentUserId = getCurrentUserId();
            UserResponse response = userManagementService.createUser(request, currentUserId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User created successfully",
                "data", response
            ));
        } catch (RuntimeException e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to create user: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).OPERATOR.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).USER.name())")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        logger.info("Fetching user: {}", id);
        
        try {
            return userManagementService.getUserById(id)
                    .map(user -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", user
                    )))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User not found"
                    )));
        } catch (Exception e) {
            logger.error("Error fetching user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch user: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all users (with optional filters)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).OPERATOR.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name())")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String projectId) {
        
        logger.info("Fetching users - role: {}, projectId: {}", role, projectId);
        
        try {
            List<UserResponse> users;
            
            if (role != null && !role.isEmpty()) {
                users = userManagementService.getUsersByRole(role);
            } else if (projectId != null && !projectId.isEmpty()) {
                users = userManagementService.getUsersByProjectId(projectId);
            } else {
                users = userManagementService.getAllUsers();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users,
                "total", users.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch users: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).OPERATOR.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name())")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        logger.info("Updating user: {}", id);
        
        try {
            String currentUserId = getCurrentUserId();
            UserResponse response = userManagementService.updateUser(id, request, currentUserId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "data", response
            ));
        } catch (RuntimeException e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to update user: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete user (Only OPERATOR can delete users)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        logger.info("Deleting user: {}", id);
        
        try {
            userManagementService.deleteUser(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));
        } catch (RuntimeException e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to delete user: " + e.getMessage()
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
