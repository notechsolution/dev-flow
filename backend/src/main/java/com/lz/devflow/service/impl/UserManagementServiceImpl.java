package com.lz.devflow.service.impl;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.dto.CreateUserRequest;
import com.lz.devflow.dto.UpdateUserRequest;
import com.lz.devflow.dto.UserResponse;
import com.lz.devflow.entity.User;
import com.lz.devflow.repository.UserRepository;
import com.lz.devflow.service.UserManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserManagementService
 */
@Service
public class UserManagementServiceImpl implements UserManagementService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserManagementServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserResponse createUser(CreateUserRequest request, String currentUserId) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setProjectIds(request.getProjectIds());
        user.setCreatedBy(currentUserId);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }
    
    @Override
    public Optional<UserResponse> getUserById(String id) {
        return userRepository.findById(id).map(this::convertToResponse);
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equals(role))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserResponse> getUsersByProjectId(String projectId) {
        return userRepository.findAll().stream()
                .filter(user -> user.getProjectIds() != null && user.getProjectIds().contains(projectId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse updateUser(String id, UpdateUserRequest request, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if username is being changed and if it's already taken
        if (!Objects.equals(user.getUsername(), request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email is being changed and if it's already taken
        if (!Objects.equals(user.getEmail(), request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        user.setProjectIds(request.getProjectIds());
        user.setUpdatedBy(currentUserId);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
    
    @Override
    public boolean hasProjectAccess(String userId, String projectId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Operators have access to all projects
        if (user.getRole() == UserRole.OPERATOR) {
            return true;
        }
        
        // Check if user has this project in their projectIds
        return user.getProjectIds() != null && user.getProjectIds().contains(projectId);
    }
    
    @Override
    public boolean isProjectAdmin(String userId, String projectId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Operators are admins of all projects
        if (user.getRole() == UserRole.OPERATOR) {
            return true;
        }
        
        // Check if user is ADMIN role and has access to this project
        return user.getRole() == UserRole.ADMIN && 
               user.getProjectIds() != null && 
               user.getProjectIds().contains(projectId);
    }
    
    @Override
    public boolean isOperator(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.isPresent() && userOpt.get().getRole() == UserRole.OPERATOR;
    }
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setProjectIds(user.getProjectIds());
        response.setCreatedBy(user.getCreatedBy());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedBy(user.getUpdatedBy());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
