package com.lz.devflow.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import com.lz.devflow.dto.CustomizeUserDetails;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    private SecurityUtils() {
    }

    public static String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            } else if (principal instanceof String stringPrincipal) {
                return stringPrincipal;
            }
        }
        return null;
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof DefaultOidcUser defaultOidcUser) {
                return defaultOidcUser.getEmail();
            }
        }
        return null;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomizeUserDetails customizeUserDetails) {
                return customizeUserDetails.getUserId();
            }
        }
        return null;
    }

    public static String fuzzyString(String accessToken) {
        if (StringUtils.isEmpty(accessToken)) {
            return accessToken;
        }
        if (accessToken.length() <= 8) {
            return "*".repeat(accessToken.length());
        }

        if (accessToken.length() <= 16) {
            return accessToken.substring(0, 2) + "*".repeat(accessToken.length() - 4) + accessToken.substring(6);
        }
        return accessToken.substring(0, 6) + "*".repeat(accessToken.length() - 12) + accessToken.substring(6 + (accessToken.length() - 12));
    }

    public static String generateToken(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
