package com.lz.devflow.config;

import com.lz.devflow.service.AIProviderConfigService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application startup listener to initialize default configurations
 */
@Component
public class ApplicationStartupListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    
    @Resource
    private AIProviderConfigService providerConfigService;
    
    /**
     * Initialize default AI provider configurations on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application is ready, initializing default configurations...");
        
        try {
            providerConfigService.initializeDefaultProviders();
            logger.info("Default AI provider configurations initialized successfully");
        } catch (Exception e) {
            logger.warn("Failed to initialize default AI provider configurations: {}", e.getMessage());
            // Don't fail application startup if initialization fails
        }
    }
}
