package com.lz.devflow.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for requirement clarification
 */
public class RequirementClarificationResponse {
    
    private List<ClarificationQuestion> questions;
    private boolean success;
    private String message;

    // Constructors
    public RequirementClarificationResponse() {
        this.questions = new ArrayList<>();
    }

    public RequirementClarificationResponse(List<ClarificationQuestion> questions, boolean success, String message) {
        this.questions = questions != null ? questions : new ArrayList<>();
        this.success = success;
        this.message = message;
    }

    // Static factory methods
    public static RequirementClarificationResponse success(List<ClarificationQuestion> questions) {
        return new RequirementClarificationResponse(questions, true, "Clarification questions generated successfully");
    }

    public static RequirementClarificationResponse error(String message) {
        return new RequirementClarificationResponse(new ArrayList<>(), false, message);
    }

    // Getters and Setters
    public List<ClarificationQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ClarificationQuestion> questions) {
        this.questions = questions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Inner class representing a single clarification question
     */
    public static class ClarificationQuestion {
        private String id;
        private String question;
        private String category;
        private String answer;

        // Constructors
        public ClarificationQuestion() {
        }

        public ClarificationQuestion(String id, String question, String category) {
            this.id = id;
            this.question = question;
            this.category = category;
            this.answer = "";
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
