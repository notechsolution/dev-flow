package com.lz.devflow.config;

import com.lz.devflow.service.PromptTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize system default prompt templates on application startup
 */
@Component
public class PromptTemplateInitializer implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateInitializer.class);
    
    private final PromptTemplateService promptTemplateService;

    public PromptTemplateInitializer(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            logger.info("Initializing system default prompt templates...");
            promptTemplateService.initializeSystemTemplates();
            logger.info("System default prompt templates initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize system default prompt templates", e);
            // Don't throw exception to prevent application startup failure
        }
    }
}
