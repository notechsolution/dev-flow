package com.lz.devflow.repository;

import com.lz.devflow.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    
    /**
     * Find all projects owned by a specific user
     */
    List<Project> findByOwnerId(String ownerId);
    
    /**
     * Find all projects where user is an admin
     */
    @Query("{'adminIds': ?0}")
    List<Project> findByAdminId(String adminId);
    
    /**
     * Find all projects where user is a member
     */
    @Query("{'memberIds': ?0}")
    List<Project> findByMemberId(String memberId);
    
    /**
     * Find projects by status
     */
    List<Project> findByStatus(String status);
    
    /**
     * Find projects by name (case-insensitive search)
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Project> findByNameContaining(String name);
}
