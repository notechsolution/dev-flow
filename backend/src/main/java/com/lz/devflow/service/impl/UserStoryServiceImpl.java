package com.lz.devflow.service.impl;

import com.lz.devflow.dto.CreateUserStoryRequest;
import com.lz.devflow.dto.UserStoryResponse;
import com.lz.devflow.entity.UserStory;
import com.lz.devflow.repository.UserStoryRepository;
import com.lz.devflow.service.UserStoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserStoryService
 */
@Service
public class UserStoryServiceImpl implements UserStoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserStoryServiceImpl.class);
    
    private final UserStoryRepository userStoryRepository;
    
    public UserStoryServiceImpl(UserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }
    
    @Override
    public UserStoryResponse createUserStory(CreateUserStoryRequest request, String currentUserId) {
        logger.info("Creating user story with title: {}", request.getTitle());
        
        UserStory userStory = new UserStory();
        copyRequestToEntity(request, userStory);
        
        userStory.setCreatedBy(currentUserId);
        userStory.setUpdatedBy(currentUserId);
        userStory.setCreatedAt(LocalDateTime.now());
        userStory.setUpdatedAt(LocalDateTime.now());
        
        UserStory saved = userStoryRepository.save(userStory);
        logger.info("User story created with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    @Override
    public Optional<UserStoryResponse> getUserStoryById(String id) {
        logger.debug("Fetching user story with ID: {}", id);
        return userStoryRepository.findById(id)
                .map(this::convertToResponse);
    }
    
    @Override
    public List<UserStoryResponse> getAllUserStories() {
        logger.debug("Fetching all user stories");
        return userStoryRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserStoryResponse> getUserStoriesByProjectId(String projectId) {
        logger.debug("Fetching user stories for project: {}", projectId);
        return userStoryRepository.findByProjectId(projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserStoryResponse> getUserStoriesByOwnerId(String ownerId) {
        logger.debug("Fetching user stories for owner: {}", ownerId);
        return userStoryRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserStoryResponse> getUserStoriesByStatus(String status) {
        logger.debug("Fetching user stories with status: {}", status);
        return userStoryRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserStoryResponse updateUserStory(String id, CreateUserStoryRequest request, String currentUserId) {
        logger.info("Updating user story with ID: {}", id);
        
        UserStory userStory = userStoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User story not found with id: " + id));
        
        copyRequestToEntity(request, userStory);
        
        userStory.setUpdatedBy(currentUserId);
        userStory.setUpdatedAt(LocalDateTime.now());
        
        UserStory updated = userStoryRepository.save(userStory);
        logger.info("User story updated: {}", id);
        
        return convertToResponse(updated);
    }
    
    @Override
    public void deleteUserStory(String id) {
        logger.info("Deleting user story with ID: {}", id);
        userStoryRepository.deleteById(id);
        logger.info("User story deleted: {}", id);
    }
    
    @Override
    public UserStoryResponse updateUserStoryStatus(String id, String status, String currentUserId) {
        logger.info("Updating status of user story {} to {}", id, status);
        
        UserStory userStory = userStoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User story not found with id: " + id));
        
        userStory.setStatus(status);
        userStory.setUpdatedBy(currentUserId);
        userStory.setUpdatedAt(LocalDateTime.now());
        
        UserStory updated = userStoryRepository.save(userStory);
        logger.info("User story status updated: {}", id);
        
        return convertToResponse(updated);
    }
    
    /**
     * Copy request data to entity
     */
    private void copyRequestToEntity(CreateUserStoryRequest request, UserStory entity) {
        entity.setProjectId(request.getProjectId());
        entity.setTitle(request.getTitle());
        entity.setTags(request.getTags());
        entity.setOwnerId(request.getOwnerId());
        entity.setOriginalRequirement(request.getOriginalRequirement());
        entity.setOptimizedRequirement(request.getOptimizedRequirement());
        entity.setUserStory(request.getUserStory());
        entity.setAcceptanceCriteria(request.getAcceptanceCriteria());
        entity.setTechnicalNotes(request.getTechnicalNotes());
        entity.setTestCases(request.getTestCases());
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        
        // Convert clarification QAs
        if (request.getClarificationQAs() != null) {
            List<UserStory.ClarificationQA> qas = request.getClarificationQAs().stream()
                    .map(req -> {
                        UserStory.ClarificationQA qa = new UserStory.ClarificationQA();
                        qa.setQuestionId(req.getQuestionId());
                        qa.setQuestion(req.getQuestion());
                        qa.setAnswer(req.getAnswer());
                        qa.setCategory(req.getCategory());
                        return qa;
                    })
                    .collect(Collectors.toList());
            entity.setClarificationQAs(qas);
        }
    }
    
    /**
     * Convert entity to response DTO
     */
    private UserStoryResponse convertToResponse(UserStory entity) {
        UserStoryResponse response = new UserStoryResponse();
        
        response.setId(entity.getId());
        response.setProjectId(entity.getProjectId());
        response.setTitle(entity.getTitle());
        response.setTags(entity.getTags());
        response.setOwnerId(entity.getOwnerId());
        response.setOriginalRequirement(entity.getOriginalRequirement());
        response.setOptimizedRequirement(entity.getOptimizedRequirement());
        response.setUserStory(entity.getUserStory());
        response.setAcceptanceCriteria(entity.getAcceptanceCriteria());
        response.setTechnicalNotes(entity.getTechnicalNotes());
        response.setTestCases(entity.getTestCases());
        response.setStatus(entity.getStatus());
        response.setPriority(entity.getPriority());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedBy(entity.getUpdatedBy());
        
        // Convert clarification QAs
        if (entity.getClarificationQAs() != null) {
            List<UserStoryResponse.ClarificationQA> qas = entity.getClarificationQAs().stream()
                    .map(entityQa -> {
                        UserStoryResponse.ClarificationQA responseQa = new UserStoryResponse.ClarificationQA();
                        responseQa.setQuestionId(entityQa.getQuestionId());
                        responseQa.setQuestion(entityQa.getQuestion());
                        responseQa.setAnswer(entityQa.getAnswer());
                        responseQa.setCategory(entityQa.getCategory());
                        return responseQa;
                    })
                    .collect(Collectors.toList());
            response.setClarificationQAs(qas);
        }
        
        return response;
    }
}
