package com.lz.devflow.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.devflow.dto.*;
import com.lz.devflow.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementation of AIService using Spring AI Alibaba with Qwen
 */
@Service
@Primary
public class QwenAIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(QwenAIServiceImpl.class);

    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final String clarificationPromptTemplate;
    private final String optimizationPromptTemplate;

    public QwenAIServiceImpl(
            DashScopeChatModel chatModel,
            @Value("classpath:prompts/requirement-clarification.st") Resource clarificationPrompt,
            @Value("classpath:prompts/requirement-optimization.st") Resource optimizationPrompt) throws IOException {
        
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        
        // Load prompt templates
        this.clarificationPromptTemplate = clarificationPrompt.getContentAsString(StandardCharsets.UTF_8);
        this.optimizationPromptTemplate = optimizationPrompt.getContentAsString(StandardCharsets.UTF_8);
        
        logger.info("QwenAIServiceImpl initialized with Spring AI Alibaba");
    }

    @Override
    public UserStoryOptimizationResponse optimizeUserStory(UserStoryOptimizationRequest request) {
        logger.info("Optimizing user story using Qwen (legacy method, delegating to new implementation)");
        
        // This is a legacy method, we can delegate to the new implementation
        // by treating it as a simple optimization without clarification
        try {
            RequirementOptimizationRequest optimizationRequest = new RequirementOptimizationRequest();
            optimizationRequest.setOriginalRequirement(request.getDescription());
            optimizationRequest.setTitle(request.getTitle());
            optimizationRequest.setProjectContext(request.getProjectContext());
            optimizationRequest.setClarificationAnswers(new ArrayList<>()); // No clarification
            
            RequirementOptimizationResponse response = optimizeRequirementWithClarification(optimizationRequest);
            
            if (response.isSuccess()) {
                return UserStoryOptimizationResponse.success(response.getOptimizedRequirement());
            } else {
                return UserStoryOptimizationResponse.error(response.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error in legacy optimizeUserStory method", e);
            return UserStoryOptimizationResponse.error("Failed to optimize user story: " + e.getMessage());
        }
    }

    @Override
    public TestCaseGenerationResponse generateTestCases(TestCaseGenerationRequest request) {
        logger.info("Generating test cases using Qwen");
        
        try {
            String description = request.getOptimizedDescription() != null && !request.getOptimizedDescription().trim().isEmpty()
                    ? request.getOptimizedDescription()
                    : request.getDescription();
            
            String promptText = String.format(
                "You are a QA expert. Generate comprehensive test cases for the following requirement:\n\n" +
                "Project Context: %s\n\n" +
                "Requirement:\n%s\n\n" +
                "Please generate 5-10 test cases covering positive, negative, edge cases, and non-functional requirements. " +
                "Return the test cases as a JSON array with each test case as a string in markdown format.",
                request.getProjectContext() != null ? request.getProjectContext() : "N/A",
                description
            );
            
            // Call Qwen using DashScopeChatModel
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            // Parse the JSON response
            List<String> testCases = parseTestCasesFromJson(content);
            
            return TestCaseGenerationResponse.success(testCases);
            
        } catch (Exception e) {
            logger.error("Error generating test cases with Qwen", e);
            return TestCaseGenerationResponse.error("Failed to generate test cases: " + e.getMessage());
        }
    }

    @Override
    public RequirementClarificationResponse generateClarificationQuestions(RequirementClarificationRequest request) {
        logger.info("Generating clarification questions using Qwen with prompt template");
        
        try {
            // Prepare the prompt using the template
            String promptText = clarificationPromptTemplate
                    .replace("{title}", request.getTitle() != null ? request.getTitle() : "N/A")
                    .replace("{projectContext}", request.getProjectContext() != null ? request.getProjectContext() : "N/A")
                    .replace("{requirement}", request.getOriginalRequirement());
            
            logger.debug("Calling Qwen with clarification prompt");
            
            // Call Qwen using DashScopeChatModel
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            logger.debug("Received response from Qwen: {}", content.substring(0, Math.min(200, content.length())));
            
            // Parse the JSON response
            List<RequirementClarificationResponse.ClarificationQuestion> questions = 
                    parseClarificationQuestionsFromJson(content);
            
            if (questions.isEmpty()) {
                logger.warn("No questions parsed from Qwen response, using fallback");
                return generateDefaultClarificationQuestions(request);
            }
            
            return RequirementClarificationResponse.success(questions);
            
        } catch (Exception e) {
            logger.error("Error generating clarification questions with Qwen", e);
            // Fallback to default questions
            return generateDefaultClarificationQuestions(request);
        }
    }

    @Override
    public RequirementOptimizationResponse optimizeRequirementWithClarification(RequirementOptimizationRequest request) {
        logger.info("Optimizing requirement with clarification using Qwen");
        
        try {
            // Format the clarification Q&A
            StringBuilder qaBuilder = new StringBuilder();
            for (RequirementOptimizationRequest.QuestionAnswer qa : request.getClarificationAnswers()) {
                qaBuilder.append(String.format("Q: %s\nA: %s\n\n", qa.getQuestion(), qa.getAnswer()));
            }
            
            // Prepare the prompt using the template
            String promptText = optimizationPromptTemplate
                    .replace("{title}", request.getTitle() != null ? request.getTitle() : "N/A")
                    .replace("{projectContext}", request.getProjectContext() != null ? request.getProjectContext() : "N/A")
                    .replace("{originalRequirement}", request.getOriginalRequirement())
                    .replace("{clarificationQA}", qaBuilder.toString());
            
            logger.debug("Calling Qwen with optimization prompt");
            
            // Call Qwen using DashScopeChatModel
            Prompt prompt = new Prompt(new UserMessage(promptText));
        
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            logger.debug("Received optimization response from Qwen");
            
            // Parse the JSON response
            return parseOptimizationResponse(content);
            
        } catch (Exception e) {
            logger.error("Error optimizing requirement with Qwen", e);
            return RequirementOptimizationResponse.error("Failed to optimize requirement: " + e.getMessage());
        }
    }

    /**
     * Parse clarification questions from JSON response
     */
    private List<RequirementClarificationResponse.ClarificationQuestion> parseClarificationQuestionsFromJson(String jsonResponse) {
        List<RequirementClarificationResponse.ClarificationQuestion> questions = new ArrayList<>();
        
        try {
            // Clean the response - remove markdown code blocks if present
            String cleaned = jsonResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
            
            // Parse JSON array
            JsonNode rootNode = objectMapper.readTree(cleaned);
            
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    String id = node.has("id") ? node.get("id").asText() : "q" + (questions.size() + 1);
                    String question = node.has("question") ? node.get("question").asText() : "";
                    String category = node.has("category") ? node.get("category").asText() : "General";
                    
                    if (!question.isEmpty()) {
                        questions.add(new RequirementClarificationResponse.ClarificationQuestion(id, question, category));
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing clarification questions JSON", e);
        }
        
        return questions;
    }

    /**
     * Parse optimization response from JSON
     */
    private RequirementOptimizationResponse parseOptimizationResponse(String jsonResponse) {
        try {
            // Clean the response - remove markdown code blocks if present
            String cleaned = jsonResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
            
            // Parse JSON object
            JsonNode rootNode = objectMapper.readTree(cleaned);
            
            String optimizedRequirement = rootNode.has("optimizedRequirement") 
                    ? rootNode.get("optimizedRequirement").asText() : "";
            String userStory = rootNode.has("userStory") 
                    ? rootNode.get("userStory").asText() : "";
            String acceptanceCriteria = rootNode.has("acceptanceCriteria") 
                    ? rootNode.get("acceptanceCriteria").asText() : "";
            String technicalNotes = rootNode.has("technicalNotes") 
                    ? rootNode.get("technicalNotes").asText() : "";
            
            return RequirementOptimizationResponse.success(
                    optimizedRequirement, userStory, acceptanceCriteria, technicalNotes);
            
        } catch (Exception e) {
            logger.error("Error parsing optimization response JSON", e);
            return RequirementOptimizationResponse.error("Failed to parse optimization response: " + e.getMessage());
        }
    }

    /**
     * Parse test cases from JSON response
     */
    private List<String> parseTestCasesFromJson(String jsonResponse) {
        List<String> testCases = new ArrayList<>();
        
        try {
            // Clean the response
            String cleaned = jsonResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
            
            // Parse JSON array
            JsonNode rootNode = objectMapper.readTree(cleaned);
            
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    testCases.add(node.asText());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing test cases JSON, using raw response", e);
            // If JSON parsing fails, return the raw response as a single test case
            testCases.add(jsonResponse);
        }
        
        return testCases;
    }

    /**
     * Generate default clarification questions as fallback
     */
    private RequirementClarificationResponse generateDefaultClarificationQuestions(RequirementClarificationRequest request) {
        logger.info("Generating default clarification questions");
        
        List<RequirementClarificationResponse.ClarificationQuestion> questions = new ArrayList<>();
        
        questions.add(new RequirementClarificationResponse.ClarificationQuestion(
            "q1", 
            "What are the specific user roles involved in this requirement?", 
            "Users & Roles"
        ));
        
        questions.add(new RequirementClarificationResponse.ClarificationQuestion(
            "q2", 
            "What are the main user goals or outcomes expected from this feature?", 
            "Goals & Outcomes"
        ));
        
        questions.add(new RequirementClarificationResponse.ClarificationQuestion(
            "q3", 
            "Are there any specific business rules or constraints that must be followed?", 
            "Business Rules"
        ));
        
        questions.add(new RequirementClarificationResponse.ClarificationQuestion(
            "q4", 
            "What are the expected inputs and outputs for this feature?", 
            "Data & Interface"
        ));
        
        questions.add(new RequirementClarificationResponse.ClarificationQuestion(
            "q5", 
            "Are there any performance or scalability requirements?", 
            "Non-functional Requirements"
        ));
        
        return RequirementClarificationResponse.success(questions);
    }
}
