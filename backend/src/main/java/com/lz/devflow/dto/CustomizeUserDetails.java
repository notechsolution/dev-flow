package com.lz.devflow.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class CustomizeUserDetails extends org.springframework.security.core.userdetails.User{

    private String userId;

    public CustomizeUserDetails(String userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
