package com.lz.devflow.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for User Story
 */
public class UserStoryResponse {
    
    private String id;
    private String projectId;
    private String title;
    private List<String> tags;
    private String ownerId;
    
    private String originalRequirement;
    private List<ClarificationQA> clarificationQAs;
    
    private String optimizedRequirement;
    private String userStory;
    private String acceptanceCriteria;
    private String technicalNotes;
    
    private List<String> testCases;
    
    private String status;
    private String priority;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Nested class for clarification Q&A
    public static class ClarificationQA {
        private String questionId;
        private String question;
        private String answer;
        private String category;
        
        public ClarificationQA() {
        }
        
        // Getters and Setters
        public String getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public void setQuestion(String question) {
            this.question = question;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public void setAnswer(String answer) {
            this.answer = answer;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getOriginalRequirement() {
        return originalRequirement;
    }
    
    public void setOriginalRequirement(String originalRequirement) {
        this.originalRequirement = originalRequirement;
    }
    
    public List<ClarificationQA> getClarificationQAs() {
        return clarificationQAs;
    }
    
    public void setClarificationQAs(List<ClarificationQA> clarificationQAs) {
        this.clarificationQAs = clarificationQAs;
    }
    
    public String getOptimizedRequirement() {
        return optimizedRequirement;
    }
    
    public void setOptimizedRequirement(String optimizedRequirement) {
        this.optimizedRequirement = optimizedRequirement;
    }
    
    public String getUserStory() {
        return userStory;
    }
    
    public void setUserStory(String userStory) {
        this.userStory = userStory;
    }
    
    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }
    
    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }
    
    public String getTechnicalNotes() {
        return technicalNotes;
    }
    
    public void setTechnicalNotes(String technicalNotes) {
        this.technicalNotes = technicalNotes;
    }
    
    public List<String> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<String> testCases) {
        this.testCases = testCases;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
