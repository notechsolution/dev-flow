package com.lz.devflow.dto;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.entity.User;

import java.util.List;
import java.util.Objects;

public class UserDTO {
    private String username;
    private UserRole role;
    private List<String> projectIds;

    public static UserDTO fromUser(User user) {
        if (Objects.isNull(user)) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setProjectIds(user.getProjectIds());
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
}
