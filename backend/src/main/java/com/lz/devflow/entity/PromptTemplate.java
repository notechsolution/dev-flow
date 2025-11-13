package com.lz.devflow.entity;

import com.lz.devflow.constant.PromptLevel;
import com.lz.devflow.constant.PromptType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Prompt Template Entity
 * 支持三级提示词：系统级、项目级、用户级
 */
@Document(collection = "prompt_templates")
@CompoundIndexes({
    @CompoundIndex(name = "idx_type_level", def = "{'type': 1, 'level': 1}"),
    @CompoundIndex(name = "idx_type_level_projectId", def = "{'type': 1, 'level': 1, 'projectId': 1}"),
    @CompoundIndex(name = "idx_type_level_userId", def = "{'type': 1, 'level': 1, 'userId': 1}")
})
public class PromptTemplate {
    
    @Id
    private String id;
    
    /**
     * 提示词名称
     */
    private String name;
    
    /**
     * 提示词类型：REQUIREMENT_CLARIFICATION, REQUIREMENT_OPTIMIZATION
     */
    private PromptType type;
    
    /**
     * 提示词级别：SYSTEM, PROJECT, USER
     */
    private PromptLevel level;
    
    /**
     * 提示词内容（模板文本）
     */
    private String content;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 项目ID（仅当level为PROJECT时有值）
     */
    private String projectId;
    
    /**
     * 用户ID（仅当level为USER时有值）
     */
    private String userId;
    
    /**
     * 是否为系统默认模板
     */
    private Boolean isDefault;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;

    // Constructors
    public PromptTemplate() {
        this.isDefault = false;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PromptType getType() {
        return type;
    }

    public void setType(PromptType type) {
        this.type = type;
    }

    public PromptLevel getLevel() {
        return level;
    }

    public void setLevel(PromptLevel level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
