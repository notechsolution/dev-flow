package com.lz.devflow.service;


import com.lz.devflow.util.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("customPermission")
public class PermissionService {
    

    public boolean hasStoryPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        String email = SecurityUtils.getCurrentUserEmail();

        return true;
    }


}
