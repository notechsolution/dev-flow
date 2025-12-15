package com.lz.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for final requirement optimization
 */
public class RequirementOptimizationRequest {
    
    @NotBlank(message = "Original requirement cannot be blank")
    private String originalRequirement;
    
    private String title;
    private String projectContext;
    
    // Project ID (optional, for project-level prompt template)
    private String projectId;
    
    // Custom prompt template ID (optional, if user wants to use specific template)
    private String promptTemplateId;
    
    // AI provider to use (dashscope, ollama, openai). If not specified, uses default provider
    private String provider;
    
    // AI model to use (e.g., qwen-turbo, gpt-4). If not specified, uses provider's default model
    private String model;
    
    @NotEmpty(message = "Clarification answers cannot be empty")
    private List<QuestionAnswer> clarificationAnswers;

    // Constructors
    public RequirementOptimizationRequest() {
    }

    public RequirementOptimizationRequest(String originalRequirement, String title, 
                                         String projectContext, List<QuestionAnswer> clarificationAnswers) {
        this.originalRequirement = originalRequirement;
        this.title = title;
        this.projectContext = projectContext;
        this.clarificationAnswers = clarificationAnswers;
    }

    // Getters and Setters
    public String getOriginalRequirement() {
        return originalRequirement;
    }

    public void setOriginalRequirement(String originalRequirement) {
        this.originalRequirement = originalRequirement;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(String projectContext) {
        this.projectContext = projectContext;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getPromptTemplateId() {
        return promptTemplateId;
    }

    public void setPromptTemplateId(String promptTemplateId) {
        this.promptTemplateId = promptTemplateId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }

    public List<QuestionAnswer> getClarificationAnswers() {
        return clarificationAnswers;
    }

    public void setClarificationAnswers(List<QuestionAnswer> clarificationAnswers) {
        this.clarificationAnswers = clarificationAnswers;
    }

    /**
     * Inner class representing a question-answer pair
     */
    public static class QuestionAnswer {
        private String questionId;
        private String question;
        private String answer;
        private String category;

        // Constructors
        public QuestionAnswer() {
        }

        public QuestionAnswer(String questionId, String question, String answer, String category) {
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
}
