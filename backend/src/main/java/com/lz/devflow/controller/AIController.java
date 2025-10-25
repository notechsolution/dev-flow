package com.lz.devflow.controller;

import com.lz.devflow.dto.UserStoryOptimizationRequest;
import com.lz.devflow.dto.UserStoryOptimizationResponse;
import com.lz.devflow.dto.TestCaseGenerationRequest;
import com.lz.devflow.dto.TestCaseGenerationResponse;
import com.lz.devflow.service.AIService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-powered operations
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Resource
    private AIService aiService;

    /**
     * Optimize user story description using AI
     * 
     * @param request the optimization request
     * @return optimized user story response
     */
    @PostMapping("/optimize-user-story")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<UserStoryOptimizationResponse> optimizeUserStory(
            @Valid @RequestBody UserStoryOptimizationRequest request) {
        
        logger.info("Received user story optimization request");
        
        try {
            UserStoryOptimizationResponse response = aiService.optimizeUserStory(request);
            
            if (response.isSuccess()) {
                logger.info("User story optimization completed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("User story optimization failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing user story optimization request", e);
            UserStoryOptimizationResponse errorResponse = 
                UserStoryOptimizationResponse.error("Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Generate test cases for user story using AI
     * 
     * @param request the test case generation request
     * @return generated test cases response
     */
    @PostMapping("/generate-test-cases")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).USER.name(), T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<TestCaseGenerationResponse> generateTestCases(
            @Valid @RequestBody TestCaseGenerationRequest request) {
        
        logger.info("Received test case generation request");
        
        try {
            TestCaseGenerationResponse response = aiService.generateTestCases(request);
            
            if (response.isSuccess()) {
                logger.info("Test case generation completed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Test case generation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing test case generation request", e);
            TestCaseGenerationResponse errorResponse = 
                TestCaseGenerationResponse.error("Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint for AI services
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("AI service health check requested");
        return ResponseEntity.ok("AI service is running");
    }

    /**
     * Debug endpoint to check current user's authorities
     * 
     * @return current user's authorities
     */
    @GetMapping("/debug/auth")
    public ResponseEntity<Object> getAuthorities() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            logger.info("Current user: {}", auth.getName());
            logger.info("Authorities: {}", auth.getAuthorities());
            return ResponseEntity.ok(java.util.Map.of(
                "user", auth.getName(),
                "authorities", auth.getAuthorities(),
                "principal", auth.getPrincipal().toString()
            ));
        } else {
            return ResponseEntity.ok("No authentication found");
        }
    }

}