package com.lz.devflow.dto;

import java.util.List;

/**
 * Response DTO for test case generation
 */
public class TestCaseGenerationResponse {
    
    private List<String> testCases;
    
    private boolean success;
    
    private String message;

    public TestCaseGenerationResponse() {}

    public TestCaseGenerationResponse(List<String> testCases, boolean success, String message) {
        this.testCases = testCases;
        this.success = success;
        this.message = message;
    }

    public static TestCaseGenerationResponse success(List<String> testCases) {
        return new TestCaseGenerationResponse(testCases, true, "Success");
    }

    public static TestCaseGenerationResponse error(String message) {
        return new TestCaseGenerationResponse(null, false, message);
    }

    public List<String> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<String> testCases) {
        this.testCases = testCases;
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
}