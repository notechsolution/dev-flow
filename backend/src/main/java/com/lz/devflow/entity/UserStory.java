package com.lz.devflow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Story Entity
 */
@Document(collection = "user_stories")
public class UserStory implements Serializable {
    
    @Id
    private String id;
    
    private String projectId;
    private String title;
    private List<String> tags;
    private String ownerId;
    
    // ID from external project management system
    private String storyId;
    
    // Original requirement from user
    private String originalRequirement;
    
    // Business context to help AI better understand the requirement
    private String projectContext;
    
    // Clarification questions and answers
    private List<ClarificationQA> clarificationQAs;
    
    // Optimized requirement from AI
    private String optimizedRequirement;
    private String userStory;
    private String acceptanceCriteria;
    private String technicalNotes;
    
    // Test cases
    private List<String> testCases;
    
    // Status and metadata
    private String status; // DRAFT, IN_PROGRESS, DONE, ARCHIVED
    private String priority; // HIGH, MEDIUM, LOW
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Constructors
    public UserStory() {
        this.tags = new ArrayList<>();
        this.clarificationQAs = new ArrayList<>();
        this.testCases = new ArrayList<>();
        this.status = "DRAFT";
        this.priority = "MEDIUM";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Nested class for clarification Q&A
    public static class ClarificationQA implements Serializable {
        private String questionId;
        private String question;
        private String answer;
        private String category;
        
        public ClarificationQA() {
        }
        
        public ClarificationQA(String questionId, String question, String answer, String category) {
            this.questionId = questionId;
            this.question = question;
            this.answer = answer;
            this.category = category;
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
    
    public String getStoryId() {
        return storyId;
    }
    
    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }
    
    public String getOriginalRequirement() {
        return originalRequirement;
    }
    
    public void setOriginalRequirement(String originalRequirement) {
        this.originalRequirement = originalRequirement;
    }
    
    public String getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(String projectContext) {
        this.projectContext = projectContext;
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
