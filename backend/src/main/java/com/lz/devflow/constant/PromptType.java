package com.lz.devflow.constant;

/**
 * Enum for Prompt Template Types
 */
public enum PromptType {
    /**
     * 需求澄清提示词
     */
    REQUIREMENT_CLARIFICATION("REQUIREMENT_CLARIFICATION", "需求澄清"),
    
    /**
     * 需求优化提示词
     */
    REQUIREMENT_OPTIMIZATION("REQUIREMENT_OPTIMIZATION", "需求优化");

    private final String code;
    private final String description;

    PromptType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PromptType fromCode(String code) {
        for (PromptType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown prompt type code: " + code);
    }
}
