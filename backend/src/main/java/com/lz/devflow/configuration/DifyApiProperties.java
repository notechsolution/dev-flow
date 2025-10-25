package com.lz.devflow.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Dify AI API
 */
@Configuration
@ConfigurationProperties(prefix = "dify.api")
public class DifyApiProperties {
    
    private String baseUrl = "https://api.dify.ai/v1";
    private String key;
    private UserStoryOptimization userStoryOptimization = new UserStoryOptimization();
    private TestCaseGeneration testCaseGeneration = new TestCaseGeneration();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UserStoryOptimization getUserStoryOptimization() {
        return userStoryOptimization;
    }

    public void setUserStoryOptimization(UserStoryOptimization userStoryOptimization) {
        this.userStoryOptimization = userStoryOptimization;
    }

    public TestCaseGeneration getTestCaseGeneration() {
        return testCaseGeneration;
    }

    public void setTestCaseGeneration(TestCaseGeneration testCaseGeneration) {
        this.testCaseGeneration = testCaseGeneration;
    }

    public static class UserStoryOptimization {
        private String workflowId;

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }
    }

    public static class TestCaseGeneration {
        private String workflowId;

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }
    }
}