package com.lz.devflow.repository;

import com.lz.devflow.constant.PromptLevel;
import com.lz.devflow.constant.PromptType;
import com.lz.devflow.entity.PromptTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PromptTemplate
 */
@Repository
public interface PromptTemplateRepository extends MongoRepository<PromptTemplate, String> {
    
    /**
     * Find by type and level
     */
    List<PromptTemplate> findByTypeAndLevel(PromptType type, PromptLevel level);
    
    /**
     * Find by type, level and enabled status
     */
    List<PromptTemplate> findByTypeAndLevelAndEnabled(PromptType type, PromptLevel level, Boolean enabled);
    
    /**
     * Find project-level templates by type and project ID
     */
    List<PromptTemplate> findByTypeAndLevelAndProjectId(PromptType type, PromptLevel level, String projectId);
    
    /**
     * Find user-level templates by type and user ID
     */
    List<PromptTemplate> findByTypeAndLevelAndUserId(PromptType type, PromptLevel level, String userId);
    
    /**
     * Find project-level templates by type, project ID and enabled status
     */
    List<PromptTemplate> findByTypeAndLevelAndProjectIdAndEnabled(
        PromptType type, PromptLevel level, String projectId, Boolean enabled);
    
    /**
     * Find user-level templates by type, user ID and enabled status
     */
    List<PromptTemplate> findByTypeAndLevelAndUserIdAndEnabled(
        PromptType type, PromptLevel level, String userId, Boolean enabled);
    
    /**
     * Find default system template by type
     */
    Optional<PromptTemplate> findByTypeAndLevelAndIsDefaultAndEnabled(
        PromptType type, PromptLevel level, Boolean isDefault, Boolean enabled);
    
    /**
     * Find all templates by user ID (for user's personal templates)
     */
    List<PromptTemplate> findByUserId(String userId);
    
    /**
     * Find all templates by project ID (for project-level templates)
     */
    List<PromptTemplate> findByProjectId(String projectId);
    
    /**
     * Check if a user template exists
     */
    boolean existsByTypeAndLevelAndUserIdAndName(
        PromptType type, PromptLevel level, String userId, String name);
    
    /**
     * Check if a project template exists
     */
    boolean existsByTypeAndLevelAndProjectIdAndName(
        PromptType type, PromptLevel level, String projectId, String name);
}
