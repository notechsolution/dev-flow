package com.lz.devflow.repository;

import com.lz.devflow.entity.UserStory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for User Story operations
 */
@Repository
public interface UserStoryRepository extends MongoRepository<UserStory, String> {
    
    /**
     * Find all user stories by project ID
     */
    List<UserStory> findByProjectId(String projectId);
    
    /**
     * Find all user stories by owner ID
     */
    List<UserStory> findByOwnerId(String ownerId);
    
    /**
     * Find all user stories by status
     */
    List<UserStory> findByStatus(String status);
    
    /**
     * Find all user stories by project ID and status
     */
    List<UserStory> findByProjectIdAndStatus(String projectId, String status);
    
    /**
     * Find all user stories by project ID and owner ID
     */
    List<UserStory> findByProjectIdAndOwnerId(String projectId, String ownerId);
}
