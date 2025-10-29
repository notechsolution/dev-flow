package com.lz.devflow.service.impl;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.dto.CreateProjectRequest;
import com.lz.devflow.dto.ProjectResponse;
import com.lz.devflow.dto.UpdateProjectRequest;
import com.lz.devflow.entity.Project;
import com.lz.devflow.entity.User;
import com.lz.devflow.repository.ProjectRepository;
import com.lz.devflow.repository.UserRepository;
import com.lz.devflow.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public ProjectResponse createProject(CreateProjectRequest request, String currentUserId) {
        // Verify user is OPERATOR
        User currentUser = userRepository.findByUsername(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!UserRole.OPERATOR.equals(currentUser.getRole())) {
            throw new RuntimeException("Only OPERATOR can create projects");
        }
        
        // Create new project
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(currentUserId);
        project.setStatus("ACTIVE");
        project.setAdminIds(request.getAdminIds() != null ? request.getAdminIds() : new ArrayList<>());
        project.setMemberIds(request.getMemberIds() != null ? request.getMemberIds() : new ArrayList<>());
        
        // Set Git Repository
        if (request.getGitRepository() != null) {
            Project.GitRepository gitRepo = new Project.GitRepository();
            gitRepo.setType(request.getGitRepository().getType());
            gitRepo.setBaseUrl(request.getGitRepository().getBaseUrl());
            gitRepo.setRepositoryIds(request.getGitRepository().getRepositoryIds());
            gitRepo.setAccessToken(request.getGitRepository().getAccessToken());
            project.setGitRepository(gitRepo);
        }
        
        // Set Project Management System
        if (request.getProjectManagementSystem() != null) {
            Project.ProjectManagementSystem pms = new Project.ProjectManagementSystem();
            pms.setType(request.getProjectManagementSystem().getType());
            pms.setSystemId(request.getProjectManagementSystem().getSystemId());
            pms.setBaseUrl(request.getProjectManagementSystem().getBaseUrl());
            pms.setAccessToken(request.getProjectManagementSystem().getAccessToken());
            project.setProjectManagementSystem(pms);
        }
        
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }
    
    @Override
    public ProjectResponse getProjectById(String projectId, String currentUserId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!canAccessProject(projectId, currentUserId)) {
            throw new RuntimeException("Access denied");
        }
        
        return toProjectResponse(project);
    }
    
    @Override
    public List<ProjectResponse> getAllProjects(String currentUserId, String nameFilter, String statusFilter) {
        User currentUser = userRepository.findByUsername(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Project> projects;
        
        // OPERATOR sees all projects
        if (UserRole.OPERATOR.equals(currentUser.getRole())) {
            projects = projectRepository.findAll();
        } else {
            // ADMIN and USER see only projects they have access to
            List<Project> ownedProjects = projectRepository.findByOwnerId(currentUserId);
            List<Project> adminProjects = projectRepository.findByAdminId(currentUserId);
            List<Project> memberProjects = projectRepository.findByMemberId(currentUserId);
            
            // Combine and deduplicate
            projects = new ArrayList<>();
            projects.addAll(ownedProjects);
            for (Project p : adminProjects) {
                if (!projects.stream().anyMatch(proj -> proj.getId().equals(p.getId()))) {
                    projects.add(p);
                }
            }
            for (Project p : memberProjects) {
                if (!projects.stream().anyMatch(proj -> proj.getId().equals(p.getId()))) {
                    projects.add(p);
                }
            }
        }
        
        // Apply filters
        if (nameFilter != null && !nameFilter.isEmpty()) {
            projects = projects.stream()
                .filter(p -> p.getName().toLowerCase().contains(nameFilter.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (statusFilter != null && !statusFilter.isEmpty()) {
            projects = projects.stream()
                .filter(p -> statusFilter.equals(p.getStatus()))
                .collect(Collectors.toList());
        }
        
        return projects.stream()
            .map(this::toProjectResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, String currentUserId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!canModifyProject(projectId, currentUserId)) {
            throw new RuntimeException("Access denied: Only OPERATOR or project ADMIN can modify");
        }
        
        // Update fields
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getAdminIds() != null) {
            project.setAdminIds(request.getAdminIds());
        }
        if (request.getMemberIds() != null) {
            project.setMemberIds(request.getMemberIds());
        }
        
        // Update Git Repository
        if (request.getGitRepository() != null) {
            Project.GitRepository gitRepo = project.getGitRepository();
            if (gitRepo == null) {
                gitRepo = new Project.GitRepository();
            }
            if (request.getGitRepository().getType() != null) {
                gitRepo.setType(request.getGitRepository().getType());
            }
            if (request.getGitRepository().getBaseUrl() != null) {
                gitRepo.setBaseUrl(request.getGitRepository().getBaseUrl());
            }
            if (request.getGitRepository().getRepositoryIds() != null) {
                gitRepo.setRepositoryIds(request.getGitRepository().getRepositoryIds());
            }
            if (request.getGitRepository().getAccessToken() != null) {
                gitRepo.setAccessToken(request.getGitRepository().getAccessToken());
            }
            project.setGitRepository(gitRepo);
        }
        
        // Update Project Management System
        if (request.getProjectManagementSystem() != null) {
            Project.ProjectManagementSystem pms = project.getProjectManagementSystem();
            if (pms == null) {
                pms = new Project.ProjectManagementSystem();
            }
            if (request.getProjectManagementSystem().getType() != null) {
                pms.setType(request.getProjectManagementSystem().getType());
            }
            if (request.getProjectManagementSystem().getSystemId() != null) {
                pms.setSystemId(request.getProjectManagementSystem().getSystemId());
            }
            if (request.getProjectManagementSystem().getBaseUrl() != null) {
                pms.setBaseUrl(request.getProjectManagementSystem().getBaseUrl());
            }
            if (request.getProjectManagementSystem().getAccessToken() != null) {
                pms.setAccessToken(request.getProjectManagementSystem().getAccessToken());
            }
            project.setProjectManagementSystem(pms);
        }
        
        project.setUpdatedAt(LocalDateTime.now());
        
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }
    
    @Override
    public void deleteProject(String projectId, String currentUserId) {
        // Verify user is OPERATOR
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!UserRole.OPERATOR.equals(currentUser.getRole())) {
            throw new RuntimeException("Only OPERATOR can delete projects");
        }
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        projectRepository.delete(project);
    }
    
    @Override
    public boolean canAccessProject(String projectId, String userName) {
        User user = userRepository.findByUsername(userName).orElse(null);
        if (user == null) {
            return false;
        }
        
        // OPERATOR can access all projects
        if (UserRole.OPERATOR.equals(user.getRole())) {
            return true;
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return false;
        }
        
        // Check if user is owner, admin, or member
        return userName.equals(project.getOwnerId())
            || (project.getAdminIds() != null && project.getAdminIds().contains(userName))
            || (project.getMemberIds() != null && project.getMemberIds().contains(userName));
    }
    
    @Override
    public boolean canModifyProject(String projectId, String userName) {
        User user = userRepository.findByUsername(userName).orElse(null);
        if (user == null) {
            return false;
        }
        
        // OPERATOR can modify all projects
        if (UserRole.OPERATOR.equals(user.getRole())) {
            return true;
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return false;
        }
        
        // Check if user is owner or admin
        return userName.equals(project.getOwnerId())
            || (project.getAdminIds() != null && project.getAdminIds().contains(userName));
    }
    
    @Override
    public ProjectResponse toProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setOwnerId(project.getOwnerId());
        response.setStatus(project.getStatus());
        response.setAdminIds(project.getAdminIds());
        response.setMemberIds(project.getMemberIds());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        
        // Convert Git Repository (mask access token)
        if (project.getGitRepository() != null) {
            ProjectResponse.GitRepositoryDTO gitDto = new ProjectResponse.GitRepositoryDTO();
            gitDto.setType(project.getGitRepository().getType());
            gitDto.setBaseUrl(project.getGitRepository().getBaseUrl());
            gitDto.setRepositoryIds(project.getGitRepository().getRepositoryIds());
            // Access token is intentionally not included in response
            response.setGitRepository(gitDto);
        }
        
        // Convert Project Management System (mask access token)
        if (project.getProjectManagementSystem() != null) {
            ProjectResponse.ProjectManagementSystemDTO pmsDto = new ProjectResponse.ProjectManagementSystemDTO();
            pmsDto.setType(project.getProjectManagementSystem().getType());
            pmsDto.setSystemId(project.getProjectManagementSystem().getSystemId());
            pmsDto.setBaseUrl(project.getProjectManagementSystem().getBaseUrl());
            // Access token is intentionally not included in response
            response.setProjectManagementSystem(pmsDto);
        }
        
        return response;
    }
}
