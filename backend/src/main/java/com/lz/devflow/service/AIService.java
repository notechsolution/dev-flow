package com.lz.devflow.service;

import com.lz.devflow.dto.*;

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
    
    /**
     * Generate clarification questions for a requirement
     * 
     * @param request the clarification request
     * @return clarification questions response
     */
    RequirementClarificationResponse generateClarificationQuestions(RequirementClarificationRequest request);
    
    /**
     * Optimize requirement based on clarification answers
     * 
     * @param request the optimization request with clarification answers
     * @return optimized requirement response
     */
    RequirementOptimizationResponse optimizeRequirementWithClarification(RequirementOptimizationRequest request);
}