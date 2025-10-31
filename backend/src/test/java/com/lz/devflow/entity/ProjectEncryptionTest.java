package com.lz.devflow.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Project entity with encrypted access tokens
 */
class ProjectEncryptionTest {
    
    @Test
    void testGitRepositoryAccessTokenEncryption() {
        // Create GitRepository instance
        Project.GitRepository gitRepo = new Project.GitRepository();
        String plainToken = "ghp_myGitHubToken123456789";
        
        // Set access token (should encrypt)
        gitRepo.setAccessToken(plainToken);
        
        // Get access token (should decrypt)
        String retrievedToken = gitRepo.getAccessToken();
        
        // Retrieved token should match original plain text
        assertEquals(plainToken, retrievedToken);
    }
    
    @Test
    void testProjectManagementSystemAccessTokenEncryption() {
        // Create ProjectManagementSystem instance
        Project.ProjectManagementSystem pms = new Project.ProjectManagementSystem();
        String plainToken = "admin:password123";
        
        // Set access token (should encrypt)
        pms.setAccessToken(plainToken);
        
        // Get access token (should decrypt)
        String retrievedToken = pms.getAccessToken();
        
        // Retrieved token should match original plain text
        assertEquals(plainToken, retrievedToken);
    }
    
    @Test
    void testNullAccessToken() {
        Project.GitRepository gitRepo = new Project.GitRepository();
        
        // Setting null should work
        gitRepo.setAccessToken(null);
        assertNull(gitRepo.getAccessToken());
    }
    
    @Test
    void testEmptyAccessToken() {
        Project.GitRepository gitRepo = new Project.GitRepository();
        
        // Setting empty string should work
        gitRepo.setAccessToken("");
        assertEquals("", gitRepo.getAccessToken());
    }
    
    @Test
    void testCompleteProjectWithEncryptedTokens() {
        // Create a complete project
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        
        // Setup Git Repository with token
        Project.GitRepository gitRepo = new Project.GitRepository();
        gitRepo.setType("GITHUB");
        gitRepo.setBaseUrl("https://api.github.com");
        gitRepo.setAccessToken("ghp_testToken123");
        project.setGitRepository(gitRepo);
        
        // Setup Project Management System with token
        Project.ProjectManagementSystem pms = new Project.ProjectManagementSystem();
        pms.setType("Zentao");
        pms.setBaseUrl("https://zentao.example.com");
        pms.setAccessToken("admin:secret123");
        project.setProjectManagementSystem(pms);
        
        // Verify tokens can be retrieved correctly
        assertEquals("ghp_testToken123", project.getGitRepository().getAccessToken());
        assertEquals("admin:secret123", project.getProjectManagementSystem().getAccessToken());
    }
    
    @Test
    void testMultipleSetAndGet() {
        Project.GitRepository gitRepo = new Project.GitRepository();
        
        // Set token multiple times
        gitRepo.setAccessToken("token1");
        assertEquals("token1", gitRepo.getAccessToken());
        
        gitRepo.setAccessToken("token2");
        assertEquals("token2", gitRepo.getAccessToken());
        
        gitRepo.setAccessToken("token3");
        assertEquals("token3", gitRepo.getAccessToken());
    }
}
