package com.lz.devflow.service;

import com.lz.devflow.dto.UserStoryOptimizationRequest;
import com.lz.devflow.dto.UserStoryOptimizationResponse;
import com.lz.devflow.dto.TestCaseGenerationRequest;
import com.lz.devflow.dto.TestCaseGenerationResponse;

/**
 * Service interface for AI-powered operations
 */
public interface AIService {
    
    /**
     * Optimize user story description using AI
     * 
     * @param request the optimization request
     * @return optimized user story response
     */
    UserStoryOptimizationResponse optimizeUserStory(UserStoryOptimizationRequest request);
    
    /**
     * Generate test cases for user story using AI
     * 
     * @param request the test case generation request
     * @return generated test cases response
     */
    TestCaseGenerationResponse generateTestCases(TestCaseGenerationRequest request);
}