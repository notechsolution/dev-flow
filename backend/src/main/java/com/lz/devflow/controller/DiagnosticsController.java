package com.lz.devflow.controller;

import com.lz.devflow.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 诊断控制器
 * 提供系统诊断和测试功能（仅管理员可访问）
 */
@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    @Autowired(required = false)
    private AIService aiService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${ai.provider:unknown}")
    private String aiProvider;

    @Value("${ai.service.implementation:unknown}")
    private String aiServiceImplementation;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${mongodb.url:unknown}")
    private String mongodbUrl;

    @Value("${spring.ai.dashscope.api-key:not-set}")
    private String dashscopeApiKey;

    @Value("${spring.ai.ollama.base-url:not-set}")
    private String ollamaBaseUrl;

    /**
     * 测试 AI 服务连接
     * POST /api/diagnostics/ai/test
     * 
     * 请求体示例：
     * {
     *   "prompt": "你好，这是一个连接测试"
     * }
     */
    @PostMapping("/ai/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testAIService(@RequestBody(required = false) Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (aiService == null) {
                result.put("status", "ERROR");
                result.put("error", "AI Service 未初始化");
                result.put("suggestion", "请检查 AI 配置是否正确");
                return ResponseEntity.status(500).body(result);
            }

            String testPrompt = (request != null && request.containsKey("prompt")) 
                ? request.get("prompt") 
                : "你好，请回复'测试成功'";
            
            result.put("provider", aiProvider);
            result.put("implementation", aiServiceImplementation);
            result.put("testPrompt", testPrompt);
            
            // 创建测试请求
            com.lz.devflow.dto.RequirementClarificationRequest testRequest = 
                new com.lz.devflow.dto.RequirementClarificationRequest();
            testRequest.setOriginalRequirement(testPrompt);
            
            long startTime = System.currentTimeMillis();
            
            // 调用 AI 服务
            com.lz.devflow.dto.RequirementClarificationResponse response = 
                aiService.generateClarificationQuestions(testRequest);
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (response.isSuccess()) {
                result.put("status", "SUCCESS");
                result.put("message", "AI 服务连接正常");
                result.put("responseTime", duration + "ms");
                result.put("questionsGenerated", response.getQuestions() != null ? response.getQuestions().size() : 0);
                result.put("sampleResponse", response.getQuestions() != null && !response.getQuestions().isEmpty() 
                    ? response.getQuestions().get(0).getQuestion() 
                    : "No questions generated");
            } else {
                result.put("status", "FAILED");
                result.put("error", response.getMessage());
                result.put("responseTime", duration + "ms");
            }
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("provider", aiProvider);
            
            // 提供详细的错误诊断信息
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Connection refused") || e.getMessage().contains("connect timed out")) {
                    result.put("suggestion", "无法连接到 AI 服务，请检查网络连接和服务地址配置");
                } else if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                    result.put("suggestion", "API Key 认证失败，请检查 API Key 是否正确");
                } else if (e.getMessage().contains("429") || e.getMessage().contains("rate limit")) {
                    result.put("suggestion", "API 调用频率超限，请稍后重试");
                } else if (e.getMessage().contains("timeout")) {
                    result.put("suggestion", "请求超时，请检查网络连接或服务状态");
                }
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试 MongoDB 连接
     * GET /api/diagnostics/mongodb/test
     */
    @GetMapping("/mongodb/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testMongoDBConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 执行 ping 命令
            mongoTemplate.executeCommand("{ ping: 1 }");
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("status", "SUCCESS");
            result.put("message", "MongoDB 连接正常");
            result.put("responseTime", duration + "ms");
            
            // 获取数据库统计信息
            try {
                org.bson.Document stats = mongoTemplate.executeCommand("{ dbStats: 1 }");
                result.put("database", stats.getString("db"));
                result.put("collections", stats.getInteger("collections"));
                result.put("objects", stats.getInteger("objects"));
                result.put("dataSize", formatBytes(stats.get("dataSize", Number.class).longValue()));
            } catch (Exception e) {
                // 忽略统计信息错误
            }
            
            // 测试写入操作（在 test_collection 中）
            try {
                Map<String, Object> testDoc = new HashMap<>();
                testDoc.put("test", "connection_test");
                testDoc.put("timestamp", System.currentTimeMillis());
                
                mongoTemplate.insert(testDoc, "connection_test");
                result.put("writeTest", "SUCCESS");
                
                // 清理测试数据
                mongoTemplate.dropCollection("connection_test");
            } catch (Exception e) {
                result.put("writeTest", "FAILED: " + e.getMessage());
            }
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            
            if (e.getMessage() != null && e.getMessage().contains("Authentication failed")) {
                result.put("suggestion", "MongoDB 认证失败，请检查用户名和密码");
            } else if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                result.put("suggestion", "无法连接到 MongoDB，请检查 MongoDB 服务是否运行");
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取配置信息（脱敏）
     * GET /api/diagnostics/config
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        // AI 配置
        Map<String, Object> aiConfig = new HashMap<>();
        aiConfig.put("provider", aiProvider);
        aiConfig.put("implementation", aiServiceImplementation);
        
        if ("qwen".equals(aiProvider)) {
            aiConfig.put("apiKeyConfigured", !dashscopeApiKey.equals("not-set") && !dashscopeApiKey.equals("dummy-api-key"));
            aiConfig.put("apiKeyPrefix", maskApiKey(dashscopeApiKey));
        } else if ("ollama".equals(aiProvider)) {
            aiConfig.put("baseUrl", ollamaBaseUrl);
        }
        
        config.put("ai", aiConfig);
        
        // MongoDB 配置
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("url", sanitizeMongoUrl(mongodbUrl));
        dbConfig.put("type", "MongoDB");
        
        config.put("database", dbConfig);
        
        // 应用配置
        Map<String, Object> appConfig = new HashMap<>();
        appConfig.put("activeProfile", activeProfile);
        appConfig.put("javaVersion", System.getProperty("java.version"));
        appConfig.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
        
        config.put("application", appConfig);
        
        return ResponseEntity.ok(config);
    }

    /**
     * 获取详细的环境信息
     * GET /api/diagnostics/environment
     */
    @GetMapping("/environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEnvironment() {
        Map<String, Object> env = new HashMap<>();
        
        // JVM 信息
        Map<String, String> jvm = new HashMap<>();
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("vendor", System.getProperty("java.vendor"));
        jvm.put("home", System.getProperty("java.home"));
        jvm.put("vmName", System.getProperty("java.vm.name"));
        jvm.put("vmVersion", System.getProperty("java.vm.version"));
        env.put("jvm", jvm);
        
        // 操作系统信息
        Map<String, String> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        env.put("os", os);
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> memory = new HashMap<>();
        memory.put("max", formatBytes(runtime.maxMemory()));
        memory.put("total", formatBytes(runtime.totalMemory()));
        memory.put("free", formatBytes(runtime.freeMemory()));
        memory.put("used", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        env.put("memory", memory);
        
        // 系统属性
        Map<String, String> system = new HashMap<>();
        system.put("userDir", System.getProperty("user.dir"));
        system.put("userHome", System.getProperty("user.home"));
        system.put("tmpDir", System.getProperty("java.io.tmpdir"));
        system.put("fileSeparator", System.getProperty("file.separator"));
        system.put("pathSeparator", System.getProperty("path.separator"));
        env.put("system", system);
        
        return ResponseEntity.ok(env);
    }

    /**
     * 生成诊断报告
     * GET /api/diagnostics/report
     */
    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateDiagnosticReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("timestamp", System.currentTimeMillis());
        report.put("generatedAt", new java.util.Date().toString());
        
        // 收集所有诊断信息
        report.put("configuration", getConfiguration().getBody());
        report.put("environment", getEnvironment().getBody());
        report.put("mongodbTest", testMongoDBConnection().getBody());
        
        // AI 测试（可能失败，捕获异常）
        try {
            Map<String, String> testRequest = new HashMap<>();
            testRequest.put("prompt", "诊断测试");
            report.put("aiTest", testAIService(testRequest).getBody());
        } catch (Exception e) {
            Map<String, String> aiTestError = new HashMap<>();
            aiTestError.put("status", "ERROR");
            aiTestError.put("error", e.getMessage());
            report.put("aiTest", aiTestError);
        }
        
        // 健康状态摘要
        Map<String, String> summary = new HashMap<>();
        summary.put("overall", "请查看各项测试结果");
        report.put("summary", summary);
        
        return ResponseEntity.ok(report);
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 隐藏 MongoDB URL 中的密码
     */
    private String sanitizeMongoUrl(String url) {
        if (url == null || url.equals("unknown") || url.equals("not-set")) {
            return "Not configured";
        }
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
    }

    /**
     * 脱敏 API Key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.equals("not-set") || apiKey.equals("dummy-api-key")) {
            return "Not configured";
        }
        if (apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
