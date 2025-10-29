package com.lz.devflow.dto;

import java.util.List;

/**
 * DTO for updating an existing project
 */
public class UpdateProjectRequest {
    
    private String name;
    private String description;
    private String status;
    private List<String> adminIds;
    private List<String> memberIds;
    private GitRepositoryDTO gitRepository;
    private ProjectManagementSystemDTO projectManagementSystem;
    
    public static class GitRepositoryDTO {
        private String type;
        private String baseUrl;
        private List<String> repositoryIds;
        private String accessToken;
        
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
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
    
    public static class ProjectManagementSystemDTO {
        private String type;
        private String systemId;
        private String baseUrl;
        private String accessToken;
        
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
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
    
    // Getters and Setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public GitRepositoryDTO getGitRepository() {
        return gitRepository;
    }
    
    public void setGitRepository(GitRepositoryDTO gitRepository) {
        this.gitRepository = gitRepository;
    }
    
    public ProjectManagementSystemDTO getProjectManagementSystem() {
        return projectManagementSystem;
    }
    
    public void setProjectManagementSystem(ProjectManagementSystemDTO projectManagementSystem) {
        this.projectManagementSystem = projectManagementSystem;
    }
}
