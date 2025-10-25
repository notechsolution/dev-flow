package com.lz.devflow.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.devflow.dto.UserStoryOptimizationRequest;
import com.lz.devflow.dto.UserStoryOptimizationResponse;
import com.lz.devflow.dto.TestCaseGenerationRequest;
import com.lz.devflow.dto.TestCaseGenerationResponse;
import com.lz.devflow.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of AIService using Dify AI Agent API
 */
@Service
public class DifyAIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(DifyAIServiceImpl.class);

    @Value("${dify.api.base-url}")
    private String difyBaseUrl;

    @Value("${dify.api.key.user-story-optimization}")
    private String difyApiKeyUserStoryOptimization;

    @Value("${dify.api.key.test-case-generation}")
    private String difyApiKeyTestCaseGeneration;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DifyAIServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public UserStoryOptimizationResponse optimizeUserStory(UserStoryOptimizationRequest request) {
        try {
            logger.info("Starting user story optimization for description: {}", 
                       request.getDescription().substring(0, Math.min(50, request.getDescription().length())) + "...");

            // Prepare request body for Dify API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", createUserStoryOptimizationInputs(request));
            requestBody.put("response_mode", "blocking");
            requestBody.put("user", "dev-flow-user");

            // Make API call to Dify
            String response = callDifyAPI(requestBody,difyApiKeyUserStoryOptimization);
            
            // Parse response and extract optimized content
            return parseUserStoryOptimizationResponse(response);

        } catch (Exception e) {
            logger.error("Error optimizing user story: ", e);
            return UserStoryOptimizationResponse.error("Failed to optimize user story: " + e.getMessage());
        }
    }

    @Override
    public TestCaseGenerationResponse generateTestCases(TestCaseGenerationRequest request) {
        try {
            logger.info("Starting test case generation for user story");

            // Prepare request body for Dify API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", createTestCaseGenerationInputs(request));
            requestBody.put("response_mode", "blocking");
            requestBody.put("user", "dev-flow-user");

            // Make API call to Dify
            String response = callDifyAPI(requestBody,difyApiKeyTestCaseGeneration);
            
            // Parse response and extract test cases
            return parseTestCaseGenerationResponse(response);

        } catch (Exception e) {
            logger.error("Error generating test cases: ", e);
            return TestCaseGenerationResponse.error("Failed to generate test cases: " + e.getMessage());
        }
    }

    private Map<String, Object> createUserStoryOptimizationInputs(UserStoryOptimizationRequest request) {
        Map<String, Object> inputs = new HashMap<>();
        
        inputs.put("story_description", Base64.getEncoder().encodeToString(request.getDescription().getBytes(StandardCharsets.UTF_8)));
        inputs.put("story_title", request.getTitle());
        
        if (request.getProjectContext() != null && !request.getProjectContext().trim().isEmpty()) {
            inputs.put("project_context", request.getProjectContext());
        }
        
        if (request.getAdditionalRequirements() != null && !request.getAdditionalRequirements().trim().isEmpty()) {
            inputs.put("additional_requirements", request.getAdditionalRequirements());
        }
        
        return inputs;
    }

    private Map<String, Object> createTestCaseGenerationInputs(TestCaseGenerationRequest request) {
        Map<String, Object> inputs = new HashMap<>();
        
        // Use optimized description if available, otherwise use original
        String description = request.getOptimizedDescription() != null && !request.getOptimizedDescription().trim().isEmpty()
                ? request.getOptimizedDescription()
                : request.getDescription();
                
        inputs.put("user_story_description", description);
        
        if (request.getProjectContext() != null && !request.getProjectContext().trim().isEmpty()) {
            inputs.put("project_context", request.getProjectContext());
        }
        
        return inputs;
    }

    private String callDifyAPI( Map<String, Object> requestBody, String difyApiKey) throws Exception {
        String url = difyBaseUrl + "/workflows/run";
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyApiKey);
        
        // Create request entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, String.class);
                
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Dify API returned status: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error calling Dify API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Dify API error: " + e.getMessage());
        }
    }

    private UserStoryOptimizationResponse parseUserStoryOptimizationResponse(String response) throws Exception {
        JsonNode rootNode = objectMapper.readTree(response);
        
        // Extract the data from Dify response
        JsonNode dataNode = rootNode.get("data");
        if (dataNode == null) {
            throw new RuntimeException("Invalid response format from Dify API");
        }
        
        // Get the outputs
        JsonNode outputsNode = dataNode.get("outputs");
        if (outputsNode == null) {
            throw new RuntimeException("No outputs in Dify API response");
        }
        
        // Extract different components from the response
        String fullResponse = outputsNode.get("description") != null ? outputsNode.get("description").asText() : "";
        
        // Parse the response to extract different sections
        // String optimizedDescription = extractSection(fullResponse, "Optimized User Story", "Acceptance Criteria");
        // String acceptanceCriteria = extractSection(fullResponse, "Acceptance Criteria", "Definition of Done");
        // String definitionOfDone = extractSection(fullResponse, "Definition of Done", null);
        
        // If sections are not found, use the full response as optimized description
        // if (optimizedDescription.isEmpty()) {
        //     optimizedDescription = fullResponse;
        // }
        
        return UserStoryOptimizationResponse.success(fullResponse);
    }

    private TestCaseGenerationResponse parseTestCaseGenerationResponse(String response) throws Exception {
        JsonNode rootNode = objectMapper.readTree(response);
        
        // Extract the data from Dify response
        JsonNode dataNode = rootNode.get("data");
        if (dataNode == null) {
            throw new RuntimeException("Invalid response format from Dify API");
        }
        
        // Get the outputs
        JsonNode outputsNode = dataNode.get("outputs");
        if (outputsNode == null) {
            throw new RuntimeException("No outputs in Dify API response");
        }
        
        String fullResponse = outputsNode.get("text") != null ? outputsNode.get("text").asText() : "";
        
        // Parse test cases from the response
        List<String> testCases = parseTestCases(fullResponse);
        
        return TestCaseGenerationResponse.success(testCases);
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Create regex patterns for section extraction
        String startPattern = "(?i)#+\\s*" + Pattern.quote(startMarker) + ".*?\\n";
        Pattern pattern;
        
        if (endMarker != null) {
            String endPattern = "(?i)#+\\s*" + Pattern.quote(endMarker);
            pattern = Pattern.compile(startPattern + "(.*?)(?=" + endPattern + "|$)", Pattern.DOTALL);
        } else {
            pattern = Pattern.compile(startPattern + "(.*?)$", Pattern.DOTALL);
        }
        
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "";
    }

    private List<String> parseTestCases(String text) {
        List<String> testCases = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return testCases;
        }
        
        // Pattern to match test case sections (## Test Case N: Title)
        Pattern testCasePattern = Pattern.compile("##\\s*Test Case \\d+:.*?(?=##\\s*Test Case \\d+:|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = testCasePattern.matcher(text);
        
        while (matcher.find()) {
            String testCase = matcher.group().trim();
            if (!testCase.isEmpty()) {
                testCases.add(testCase);
            }
        }
        
        // If no structured test cases found, try to split by numbered items
        if (testCases.isEmpty()) {
            String[] lines = text.split("\\n");
            StringBuilder currentTestCase = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                if (line.matches("^\\d+\\..*") || line.matches("^Test Case \\d+.*")) {
                    if (currentTestCase.length() > 0) {
                        testCases.add(currentTestCase.toString().trim());
                        currentTestCase = new StringBuilder();
                    }
                    currentTestCase.append(line).append("\n");
                } else if (currentTestCase.length() > 0 && !line.isEmpty()) {
                    currentTestCase.append(line).append("\n");
                }
            }
            
            if (currentTestCase.length() > 0) {
                testCases.add(currentTestCase.toString().trim());
            }
        }
        
        // If still no test cases found, return the full text as a single test case
        if (testCases.isEmpty() && !text.trim().isEmpty()) {
            testCases.add(text.trim());
        }
        
        return testCases;
    }
}