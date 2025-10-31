package com.lz.devflow.entity;

import com.lz.devflow.util.CryptoUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project entity
 */
@Document(collection = "projects")
public class Project {
    
    @Id
    private String id;
    
    private String name;
    
    private String description;
    
    private String ownerId;
    
    private List<String> adminIds; // Project admins
    
    private List<String> memberIds; // Project members
    
    // Git Repository Information
    private GitRepository gitRepository;
    
    // Project Management System Information
    private ProjectManagementSystem projectManagementSystem;
    
    private String status; // ACTIVE, ARCHIVED, DELETED
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
    
    // Nested class for Git Repository
    public static class GitRepository {
        private String type; // GITHUB, GITLAB, BITBUCKET, AZURE_DEVOPS
        private String baseUrl;
        private List<String> repositoryIds; // List of repository IDs/names
        private String accessToken; // Encrypted access token
        
        public GitRepository() {
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public List<String> getRepositoryIds() {
            return repositoryIds;
        }
        
        public void setRepositoryIds(List<String> repositoryIds) {
            this.repositoryIds = repositoryIds;
        }
        
        public String getAccessToken() {
            // Decrypt when getting
            return CryptoUtil.decrypt(accessToken);
        }
        
        public void setAccessToken(String accessToken) {
            // Encrypt when setting
            this.accessToken = CryptoUtil.encrypt(accessToken);
        }
    }
    
    // Nested class for Project Management System
    public static class ProjectManagementSystem {
        private String type; // Zentao, AZURE_DEVOPS, GITHUB_ISSUES, TRELLO
        private String systemId; // Project ID in the PM system
        private String baseUrl;
        private String accessToken; // Encrypted access token
        
        public ProjectManagementSystem() {
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getSystemId() {
            return systemId;
        }
        
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getAccessToken() {
            // Decrypt when getting
            return CryptoUtil.decrypt(accessToken);
        }
        
        public void setAccessToken(String accessToken) {
            // Encrypt when setting
            this.accessToken = CryptoUtil.encrypt(accessToken);
        }
    }
    
    // Constructors
    public Project() {
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public List<String> getAdminIds() {
        return adminIds;
    }
    
    public void setAdminIds(List<String> adminIds) {
        this.adminIds = adminIds;
    }
    
    public List<String> getMemberIds() {
        return memberIds;
    }
    
    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }
    
    public GitRepository getGitRepository() {
        return gitRepository;
    }
    
    public void setGitRepository(GitRepository gitRepository) {
        this.gitRepository = gitRepository;
    }
    
    public ProjectManagementSystem getProjectManagementSystem() {
        return projectManagementSystem;
    }
    
    public void setProjectManagementSystem(ProjectManagementSystem projectManagementSystem) {
        this.projectManagementSystem = projectManagementSystem;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
