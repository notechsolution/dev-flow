package com.lz.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO for creating a new project
 */
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    private List<String> adminIds;
    
    private List<String> memberIds;
    
    // Git Repository Information
    private GitRepositoryDTO gitRepository;
    
    // Project Management System Information
    private ProjectManagementSystemDTO projectManagementSystem;
    
    public static class GitRepositoryDTO {
        private String type; // GITHUB, GITLAB, BITBUCKET, AZURE_DEVOPS
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
        private String type; // JIRA, AZURE_DEVOPS, GITHUB_ISSUES, TRELLO
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
