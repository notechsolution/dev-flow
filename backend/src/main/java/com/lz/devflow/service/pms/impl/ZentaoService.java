package com.lz.devflow.service.pms.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.devflow.dto.zentao.*;
import com.lz.devflow.entity.UserStory;
import com.lz.devflow.service.pms.ProjectManagementSystemService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Zentao Project Management System Service Implementation
 * Integrates with Zentao's RESTful API for story management
 */
@Service
public class ZentaoService implements ProjectManagementSystemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ZentaoService.class);
    private static final String TYPE = "Zentao";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    
    // Cache for tokens to avoid frequent token requests
    private final Map<String, TokenCache> tokenCache = new HashMap<>();
    
    public ZentaoService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        // Initialize flexmark-java parser and renderer
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public String createStory(UserStory userStory, String systemId, String baseUrl, String accessToken) {
        logger.info("Creating story in Zentao for user story: {}", userStory.getId());
        
        try {
            // Get or refresh token
            String token = getToken(baseUrl, accessToken);
            
            // Prepare the story request
            ZentaoStoryRequest storyRequest = buildStoryRequest(userStory, systemId);
            
            // Create story in Zentao
            String storyId = createStoryInZentao(baseUrl, token, systemId, storyRequest);
            
            logger.info("Successfully created story in Zentao with ID: {}", storyId);
            return storyId;
            
        } catch (Exception e) {
            logger.error("Failed to create story in Zentao for user story: {}", userStory.getId(), e);
            throw new RuntimeException("Failed to create story in Zentao: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get authentication token from Zentao
     */
    private String getToken(String baseUrl, String accessToken) {
        // String cacheKey = baseUrl + ":" + accessToken;
        
        // // Check if we have a valid cached token
        // TokenCache cached = tokenCache.get(cacheKey);
        // if (cached != null && !cached.isExpired()) {
        //     logger.debug("Using cached Zentao token");
        //     return cached.token;
        // }
        
        logger.info("Requesting new token from Zentao");
        
        try {
            // According to Zentao API docs, the token endpoint is: /api.php/v1/tokens
            String tokenUrl = normalizeUrl(baseUrl) + "/api.php/v1/tokens";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request body with account and password
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("account", extractAccount(accessToken));
            requestBody.put("password", extractPassword(accessToken));
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String token = jsonNode.path("token").asText();
                
                if (!StringUtils.hasText(token)) {
                    throw new RuntimeException("Token not found in response");
                }
                
                // // Cache the token (Zentao tokens typically expire in 2 hours)
                // long expireTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000); // 2 hours
                // tokenCache.put(cacheKey, new TokenCache(token, expireTime));
                
                logger.info("Successfully obtained Zentao token");
                return token;
            } else {
                throw new RuntimeException("Failed to get token, status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting Zentao token", e);
            throw new RuntimeException("Failed to get Zentao token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create story in Zentao via API
     */
    private String createStoryInZentao(String baseUrl, String token, String productId, ZentaoStoryRequest storyRequest) {
        try {
            // According to Zentao API docs, the story creation endpoint is: /api.php/v1/products/{productID}/stories
            String createUrl = normalizeUrl(baseUrl) + "/api.php/v1/stories" ;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", token);
            
            HttpEntity<ZentaoStoryRequest> request = new HttpEntity<>(storyRequest, headers);
            
            logger.debug("Creating story in Zentao at URL: {}", createUrl);
            
            ResponseEntity<String> response = restTemplate.exchange(
                createUrl,
                HttpMethod.POST,
                request,
                String.class
            );
            
            logger.debug("Response from Zentao: {}", response.getBody());
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String storyId = jsonNode.path("id").asText();
                
                if (!StringUtils.hasText(storyId)) {
                    throw new RuntimeException("Story ID not found in response");
                }
                
                return storyId;
            } else {
                throw new RuntimeException("Failed to create story, status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error creating story in Zentao", e);
            throw new RuntimeException("Failed to create story in Zentao: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build Zentao story request from UserStory entity
     */
    private ZentaoStoryRequest buildStoryRequest(UserStory userStory, String productId) {
        ZentaoStoryRequest request = new ZentaoStoryRequest();
        
        // Required fields
        request.setProduct(productId);
        request.setTitle(userStory.getTitle());
        
        // Build story description combining all available information
        StringBuilder spec = new StringBuilder();
        
        if (StringUtils.hasText(userStory.getUserStory())) {
            spec.append("**用户故事**\n").append(userStory.getUserStory()).append("\n\n");
        }
        
        if (StringUtils.hasText(userStory.getOptimizedRequirement())) {
            spec.append("**需求描述**\n").append(userStory.getOptimizedRequirement()).append("\n\n");
        } else if (StringUtils.hasText(userStory.getOriginalRequirement())) {
            spec.append("**需求描述**\n").append(userStory.getOriginalRequirement()).append("\n\n");
        }
        
        if (StringUtils.hasText(userStory.getTechnicalNotes())) {
            spec.append("**技术备注**\n").append(userStory.getTechnicalNotes()).append("\n\n");
        }
        
        request.setSpec(convertMarkdownToHtml(spec.toString()));
        
        // Acceptance criteria as verification
        if (StringUtils.hasText(userStory.getAcceptanceCriteria())) {
            request.setVerify(convertMarkdownToHtml(userStory.getAcceptanceCriteria()));
        }
        
        // Story type - default to "story"
        // request.setType("story");
        request.setCategory("feature");
        
        // Priority mapping: HIGH -> 1, MEDIUM -> 2, LOW -> 3
        String priority = "3"; // Default to low
        if (userStory.getPriority() != null) {
            switch (userStory.getPriority().toUpperCase()) {
                case "HIGH":
                    priority = "1";
                    break;
                case "MEDIUM":
                    priority = "2";
                    break;
                case "LOW":
                    priority = "3";
                    break;
            }
        }
        request.setPri(priority);
        
        // Source
        request.setSourceNote("Created from DevFlow system");
        
        // Keywords from tags
        if (userStory.getTags() != null && !userStory.getTags().isEmpty()) {
            String keywords = String.join(",", userStory.getTags());
            request.setKeywords(keywords);
        }
        
        // // Status - default to "reviewing"
        // request.setStatus("reviewing");
        
        // // Stage - default to "wait"
        // request.setStage("projected");
        
        return request;
    }

    /**
     * Converts a Markdown string to its HTML representation.
     * @param markdown The Markdown string to convert.
     * @return The corresponding HTML string.
     */
    private String convertMarkdownToHtml(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }
    
    /**
     * Normalize base URL (remove trailing slash)
     */
    private String normalizeUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    /**
     * Extract account from access token
     * Access token format: account:password
     */
    private String extractAccount(String accessToken) {
        if (accessToken.contains(":")) {
            return accessToken.split(":")[0];
        }
        throw new IllegalArgumentException("Invalid access token format. Expected format: account:password");
    }
    
    /**
     * Extract password from access token
     * Access token format: account:password
     */
    private String extractPassword(String accessToken) {
        if (accessToken.contains(":")) {
            String[] parts = accessToken.split(":", 2);
            return parts.length > 1 ? parts[1] : "";
        }
        throw new IllegalArgumentException("Invalid access token format. Expected format: account:password");
    }
    
    /**
     * Token cache inner class
     */
    private static class TokenCache {
        private final String token;
        private final long expireTime;
        
        public TokenCache(String token, long expireTime) {
            this.token = token;
            this.expireTime = expireTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() >= expireTime;
        }
    }
}