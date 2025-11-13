package com.lz.devflow.service;

import com.lz.devflow.constant.PromptType;
import com.lz.devflow.dto.PromptTemplateRequest;
import com.lz.devflow.dto.PromptTemplateResponse;

import java.util.List;

/**
 * Service interface for Prompt Template management
 */
public interface PromptTemplateService {
    
    /**
     * 获取指定类型的提示词模板（按优先级：用户级 > 项目级 > 系统级）
     * 
     * @param type 提示词类型
     * @param userId 用户ID（可选）
     * @param projectId 项目ID（可选）
     * @return 提示词模板响应
     */
    PromptTemplateResponse getEffectivePromptTemplate(PromptType type, String userId, String projectId);
    
    /**
     * 获取系统默认提示词模板
     * 
     * @param type 提示词类型
     * @return 提示词模板响应
     */
    PromptTemplateResponse getSystemDefaultTemplate(PromptType type);
    
    /**
     * 获取项目级别的提示词模板列表
     * 
     * @param projectId 项目ID
     * @param type 提示词类型（可选）
     * @return 提示词模板列表
     */
    List<PromptTemplateResponse> getProjectTemplates(String projectId, PromptType type);
    
    /**
     * 获取用户级别的提示词模板列表
     * 
     * @param userId 用户ID
     * @param type 提示词类型（可选）
     * @return 提示词模板列表
     */
    List<PromptTemplateResponse> getUserTemplates(String userId, PromptType type);
    
    /**
     * 获取所有系统级别的提示词模板
     * 
     * @param type 提示词类型（可选）
     * @return 提示词模板列表
     */
    List<PromptTemplateResponse> getSystemTemplates(PromptType type);
    
    /**
     * 创建提示词模板
     * 
     * @param request 提示词模板请求
     * @param currentUserId 当前用户ID
     * @return 创建的提示词模板
     */
    PromptTemplateResponse createTemplate(PromptTemplateRequest request, String currentUserId);
    
    /**
     * 更新提示词模板
     * 
     * @param id 模板ID
     * @param request 提示词模板请求
     * @param currentUserId 当前用户ID
     * @return 更新后的提示词模板
     */
    PromptTemplateResponse updateTemplate(String id, PromptTemplateRequest request, String currentUserId);
    
    /**
     * 删除提示词模板
     * 
     * @param id 模板ID
     * @param currentUserId 当前用户ID
     */
    void deleteTemplate(String id, String currentUserId);
    
    /**
     * 根据ID获取提示词模板
     * 
     * @param id 模板ID
     * @return 提示词模板响应
     */
    PromptTemplateResponse getTemplateById(String id);
    
    /**
     * 初始化系统默认提示词模板
     */
    void initializeSystemTemplates();
}
