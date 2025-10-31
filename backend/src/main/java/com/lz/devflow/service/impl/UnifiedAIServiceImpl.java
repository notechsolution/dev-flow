package com.lz.devflow.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.devflow.dto.*;
import com.lz.devflow.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Unified AI Service implementation supporting multiple AI providers
 * (Qwen/DashScope, Ollama, OpenAI, etc.)
 * 
 * This implementation uses Spring AI's ChatModel interface which allows
 * switching between different providers via configuration.
 */
@Service
@Primary
@ConditionalOnProperty(name = "ai.service.implementation", havingValue = "unified", matchIfMissing = true)
public class UnifiedAIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedAIServiceImpl.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final String clarificationPromptTemplate;
    private final String optimizationPromptTemplate;
    private final String providerType;

    public UnifiedAIServiceImpl(
            ChatModel chatModel,
            @Value("classpath:prompts/requirement-clarification.st") Resource clarificationPrompt,
            @Value("classpath:prompts/requirement-optimization.st") Resource optimizationPrompt,
            @Value("${ai.provider:qwen}") String providerType) throws IOException {
        
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        this.providerType = providerType;
        
        // Load prompt templates
        this.clarificationPromptTemplate = clarificationPrompt.getContentAsString(StandardCharsets.UTF_8);
        this.optimizationPromptTemplate = optimizationPrompt.getContentAsString(StandardCharsets.UTF_8);
        
        logger.info("UnifiedAIServiceImpl initialized with provider: {}, model: {}", 
                    providerType, chatModel.getClass().getSimpleName());
    }

    @Override
    public UserStoryOptimizationResponse optimizeUserStory(UserStoryOptimizationRequest request) {
        logger.info("Optimizing user story using {} provider", providerType);
        
        try {
            RequirementOptimizationRequest optimizationRequest = new RequirementOptimizationRequest();
            optimizationRequest.setOriginalRequirement(request.getDescription());
            optimizationRequest.setTitle(request.getTitle());
            optimizationRequest.setProjectContext(request.getProjectContext());
            optimizationRequest.setClarificationAnswers(new ArrayList<>());
            
            RequirementOptimizationResponse response = optimizeRequirementWithClarification(optimizationRequest);
            
            if (response.isSuccess()) {
                return UserStoryOptimizationResponse.success(response.getOptimizedRequirement());
            } else {
                return UserStoryOptimizationResponse.error(response.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error optimizing user story", e);
            return UserStoryOptimizationResponse.error("Failed to optimize user story: " + e.getMessage());
        }
    }

    @Override
    public TestCaseGenerationResponse generateTestCases(TestCaseGenerationRequest request) {
        logger.info("Generating test cases using {} provider", providerType);
        
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
            
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            List<String> testCases = parseTestCasesFromJson(content);
            
            return TestCaseGenerationResponse.success(testCases);
            
        } catch (Exception e) {
            logger.error("Error generating test cases", e);
            return TestCaseGenerationResponse.error("Failed to generate test cases: " + e.getMessage());
        }
    }

    @Override
    public RequirementClarificationResponse generateClarificationQuestions(RequirementClarificationRequest request) {
        logger.info("Generating clarification questions using {} provider", providerType);
        
        try {
            String promptText = clarificationPromptTemplate
                    .replace("{title}", request.getTitle() != null ? request.getTitle() : "N/A")
                    .replace("{projectContext}", request.getProjectContext() != null ? request.getProjectContext() : "N/A")
                    .replace("{requirement}", request.getOriginalRequirement());
            
            logger.debug("Calling AI provider with clarification prompt");
            
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            logger.debug("Received response from AI: {}", content.substring(0, Math.min(200, content.length())));
            
            List<RequirementClarificationResponse.ClarificationQuestion> questions = 
                    parseClarificationQuestionsFromJson(content);
            
            if (questions.isEmpty()) {
                logger.warn("No questions parsed from AI response, using fallback");
                return generateDefaultClarificationQuestions(request);
            }
            
            return RequirementClarificationResponse.success(questions);
            
        } catch (Exception e) {
            logger.error("Error generating clarification questions", e);
            return generateDefaultClarificationQuestions(request);
        }
    }

    @Override
    public RequirementOptimizationResponse optimizeRequirementWithClarification(RequirementOptimizationRequest request) {
        logger.info("Optimizing requirement with clarification using {} provider", providerType);
        
        try {
            StringBuilder qaBuilder = new StringBuilder();
            for (RequirementOptimizationRequest.QuestionAnswer qa : request.getClarificationAnswers()) {
                qaBuilder.append(String.format("Q: %s\nA: %s\n\n", qa.getQuestion(), qa.getAnswer()));
            }
            
            String promptText = optimizationPromptTemplate
                    .replace("{title}", request.getTitle() != null ? request.getTitle() : "N/A")
                    .replace("{projectContext}", request.getProjectContext() != null ? request.getProjectContext() : "N/A")
                    .replace("{originalRequirement}", request.getOriginalRequirement())
                    .replace("{clarificationQA}", qaBuilder.toString());
            
            logger.debug("Calling AI provider with optimization prompt");
            
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            logger.debug("Received optimization response from AI");
            
            return parseOptimizationResponse(content);
            
        } catch (Exception e) {
            logger.error("Error optimizing requirement", e);
            return RequirementOptimizationResponse.error("Failed to optimize requirement: " + e.getMessage());
        }
    }

    /**
     * Parse clarification questions from JSON response
     */
    private List<RequirementClarificationResponse.ClarificationQuestion> parseClarificationQuestionsFromJson(String jsonResponse) {
        List<RequirementClarificationResponse.ClarificationQuestion> questions = new ArrayList<>();
        
        try {
            String cleaned = cleanJsonResponse(jsonResponse);
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
            String cleaned = cleanJsonResponse(jsonResponse);
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
            String cleaned = cleanJsonResponse(jsonResponse);
            JsonNode rootNode = objectMapper.readTree(cleaned);
            
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    testCases.add(node.asText());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing test cases JSON, using raw response", e);
            testCases.add(jsonResponse);
        }
        
        return testCases;
    }

    /**
     * Clean JSON response by removing markdown code blocks
     */
    private String cleanJsonResponse(String jsonResponse) {
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
        return cleaned.trim();
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
