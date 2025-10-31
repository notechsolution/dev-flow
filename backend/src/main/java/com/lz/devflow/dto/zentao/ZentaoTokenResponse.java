package com.lz.devflow.dto.zentao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zentao Token Response DTO
 */
public class ZentaoTokenResponse {
    
    private String token;
    
    @JsonProperty("expire_time")
    private Long expireTime;
    
    public ZentaoTokenResponse() {
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }
}
