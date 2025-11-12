package com.lz.devflow.dto;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class UserDTO {
    private String id;
    private String username;
    private UserRole role;
    private List<String> projectIds;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDTO fromUser(User user) {
        if (Objects.isNull(user)) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setProjectIds(user.getProjectIds());
        userDTO.setEmail(user.getEmail());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }

    public User toUser() {
        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        user.setProjectIds(projectIds);
        return user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<String> projectIds) {
        this.projectIds = projectIds;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
