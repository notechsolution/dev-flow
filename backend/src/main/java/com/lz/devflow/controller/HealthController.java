package com.lz.devflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 健康检查控制器
 * 提供应用、数据库、AI 服务和系统资源的健康状态检查
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${ai.provider:unknown}")
    private String aiProvider;

    @Value("${mongodb.url:unknown}")
    private String mongodbUrl;

    @Value("${mongodb.database.name:devflow}")
    private String databaseName;

    /**
     * 综合健康检查
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 应用状态
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            
            // 数据库状态
            Map<String, Object> dbHealth = checkDatabaseInternal();
            health.put("database", dbHealth);
            
            // AI 服务状态
            health.put("ai", checkAIServiceInternal());
            
            // 系统信息
            health.put("system", getSystemInfoInternal());
            
            // 如果数据库检查失败，整体状态标记为 DEGRADED
            if ("DOWN".equals(dbHealth.get("status"))) {
                health.put("status", "DEGRADED");
            }
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * 检查数据库连接
     * GET /api/health/database
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> dbHealth = checkDatabaseInternal();
        int statusCode = "UP".equals(dbHealth.get("status")) ? 200 : 503;
        return ResponseEntity.status(statusCode).body(dbHealth);
    }

    /**
     * 检查 AI 服务配置
     * GET /api/health/ai
     */
    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> checkAIService() {
        return ResponseEntity.ok(checkAIServiceInternal());
    }

    /**
     * 获取系统信息
     * GET /api/health/system
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        return ResponseEntity.ok(getSystemInfoInternal());
    }

    /**
     * 简单的 ping 端点，用于快速健康检查
     * GET /api/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    // ==================== 内部方法 ====================

    /**
     * 内部方法：检查 MongoDB 连接
     */
    private Map<String, Object> checkDatabaseInternal() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // 尝试执行一个简单的 ping 命令
            mongoTemplate.executeCommand("{ ping: 1 }");
            
            dbHealth.put("status", "UP");
            dbHealth.put("type", "MongoDB");
            dbHealth.put("database", databaseName);
            
            // 隐藏敏感信息的连接 URL
            String sanitizedUrl = sanitizeMongoUrl(mongodbUrl);
            dbHealth.put("url", sanitizedUrl);
            
            // 获取集合信息
            try {
                Set<String> collections = mongoTemplate.getDb().listCollectionNames().into(new java.util.HashSet<>());
                dbHealth.put("collections", collections);
                dbHealth.put("collectionCount", collections.size());
            } catch (Exception e) {
                dbHealth.put("collections", "Unable to retrieve");
            }
            
            // 获取数据库统计信息
            try {
                org.bson.Document stats = mongoTemplate.executeCommand("{ dbStats: 1 }");
                dbHealth.put("dataSize", formatBytes(stats.get("dataSize", Number.class).longValue()));
                dbHealth.put("storageSize", formatBytes(stats.get("storageSize", Number.class).longValue()));
                dbHealth.put("indexes", stats.get("indexes"));
                dbHealth.put("objects", stats.get("objects"));
            } catch (Exception e) {
                // 忽略统计信息错误
            }
            
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            dbHealth.put("errorType", e.getClass().getSimpleName());
            dbHealth.put("suggestion", "请检查 MongoDB 服务是否运行，连接配置是否正确");
        }
        
        return dbHealth;
    }

    /**
     * 内部方法：检查 AI 服务配置
     */
    private Map<String, Object> checkAIServiceInternal() {
        Map<String, Object> aiHealth = new HashMap<>();
        aiHealth.put("provider", aiProvider);
        aiHealth.put("status", "CONFIGURED");
        
        if ("ollama".equals(aiProvider)) {
            aiHealth.put("type", "Local Model");
            aiHealth.put("note", "请确保 Ollama 服务正在运行在配置的地址上");
            aiHealth.put("checkCommand", "curl http://localhost:11434/api/tags");
        } else if ("qwen".equals(aiProvider)) {
            aiHealth.put("type", "Cloud Service");
            aiHealth.put("provider", "Alibaba DashScope");
            aiHealth.put("note", "请确保 DASHSCOPE_API_KEY 已正确配置");
        } else {
            aiHealth.put("status", "UNKNOWN");
            aiHealth.put("warning", "未识别的 AI 提供商: " + aiProvider);
        }
        
        return aiHealth;
    }

    /**
     * 内部方法：获取系统信息
     */
    private Map<String, Object> getSystemInfoInternal() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // 内存信息
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        
        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("freeMemory", formatBytes(freeMemory));
        systemInfo.put("totalMemory", formatBytes(totalMemory));
        systemInfo.put("maxMemory", formatBytes(maxMemory));
        systemInfo.put("usedMemory", formatBytes(usedMemory));
        systemInfo.put("memoryUsagePercent", String.format("%.2f%%", (usedMemory * 100.0 / totalMemory)));
        
        // Java 信息
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("javaVendor", System.getProperty("java.vendor"));
        systemInfo.put("javaHome", System.getProperty("java.home"));
        
        // 操作系统信息
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("osArch", System.getProperty("os.arch"));
        
        // 运行时间
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        systemInfo.put("uptime", formatDuration(uptime));
        systemInfo.put("uptimeMs", uptime);
        
        return systemInfo;
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化字节数为可读格式
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 格式化持续时间
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * 隐藏 MongoDB URL 中的敏感信息
     */
    private String sanitizeMongoUrl(String url) {
        if (url == null || url.equals("unknown")) {
            return "Not configured";
        }
        
        // 隐藏密码：mongodb://user:password@host -> mongodb://user:***@host
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
    }
}
