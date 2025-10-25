package com.lz.devflow.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.concurrent.TimeUnit;

@Configuration
@Profile("!test")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${mongodb.url}")
    private String mongodbUrl;
    @Value("${mongodb.database.name}")
    private String mongodbDatabaseName;

    private final Logger logger = LoggerFactory.getLogger(MongoConfiguration.class);


    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongodbUrl);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings((ConnectionPoolSettings.Builder builder) -> {
                    builder.maxSize(100) //connections count
                            .minSize(5)
                            .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                            .maxConnectionIdleTime(3, TimeUnit.MINUTES);
                })
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(3, TimeUnit.SECONDS);
                    builder.readTimeout(30, TimeUnit.SECONDS);
                })
                .retryWrites(true)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    protected String getDatabaseName() {
        return mongodbDatabaseName;
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
