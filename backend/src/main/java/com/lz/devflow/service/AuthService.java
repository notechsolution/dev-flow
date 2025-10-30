package com.lz.devflow.service;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.dto.LoginRequest;
import com.lz.devflow.dto.LoginResponse;
import com.lz.devflow.dto.RegisterRequest;
import com.lz.devflow.dto.RegisterResponse;
import com.lz.devflow.dto.UserDTO;
import com.lz.devflow.entity.User;
import com.lz.devflow.exception.BadRequestException;
import com.lz.devflow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse authenticate(LoginRequest request, HttpServletRequest httpRequest) {
         try {
        // Use Spring Security's authentication
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        // Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Create session
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        
        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        return new LoginResponse(true, "Login successful", UserDTO.fromUser(user));
        
    } catch (AuthenticationException e) {
        throw new BadCredentialsException("Invalid credentials");
    }
    }

    public User getCurrentUser(HttpServletRequest request) {
       // Get authentication from SecurityContext
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication != null && authentication.isAuthenticated() 
        && !"anonymousUser".equals(authentication.getName())) {
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
    
    throw new RuntimeException("No authenticated user");
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        long userCount = userRepository.count();
        if (userCount == 0) {
            user.setRole(UserRole.OPERATOR);
        } else {
            user.setRole(UserRole.USER);
        }
        user.setProjectIds(new ArrayList<>());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);

        // Don't return password in response
        savedUser.setPassword(null);

        return new RegisterResponse(true, "User registered successfully", savedUser);
    }
}
