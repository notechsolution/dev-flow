package com.lz.devflow.repository;

import com.lz.devflow.entity.AIProviderConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AI Provider Configuration
 */
@Repository
public interface AIProviderConfigRepository extends MongoRepository<AIProviderConfig, String> {
    
    /**
     * Find provider config by provider name
     */
    Optional<AIProviderConfig> findByProvider(String provider);
    
    /**
     * Find all enabled providers
     */
    List<AIProviderConfig> findByEnabledTrue();
    
    /**
     * Check if provider exists
     */
    boolean existsByProvider(String provider);
}
