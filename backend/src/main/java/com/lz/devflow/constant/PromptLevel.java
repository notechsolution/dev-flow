package com.lz.devflow.constant;

/**
 * Enum for Prompt Template Levels
 */
public enum PromptLevel {
    /**
     * 系统级别提示词（默认）
     */
    SYSTEM("SYSTEM", "系统级别"),
    
    /**
     * 项目级别提示词
     */
    PROJECT("PROJECT", "项目级别"),
    
    /**
     * 用户级别提示词
     */
    USER("USER", "用户级别");

    private final String code;
    private final String description;

    PromptLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PromptLevel fromCode(String code) {
        for (PromptLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown prompt level code: " + code);
    }
}
