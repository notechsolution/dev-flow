package com.lz.devflow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 版本信息控制器
 * 提供应用版本、构建时间等信息
 */
@RestController
@RequestMapping("/api/version")
public class VersionController {

    @Value("${application.version:unknown}")
    private String version;

    @Value("${application.build.time:unknown}")
    private String buildTime;

    @Value("${application.name:DevFlow}")
    private String applicationName;

    /**
     * 获取应用版本信息
     * GET /api/version
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getVersion() {
        Map<String, Object> versionInfo = new HashMap<>();
        
        versionInfo.put("application", applicationName);
        versionInfo.put("version", version);
        versionInfo.put("buildTime", buildTime);
        versionInfo.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(versionInfo);
    }

    /**
     * 获取简化的版本信息（用于UI显示）
     * GET /api/version/simple
     */
    @GetMapping("/simple")
    public ResponseEntity<Map<String, String>> getSimpleVersion() {
        Map<String, String> simpleVersion = new HashMap<>();
        
        simpleVersion.put("version", version);
        simpleVersion.put("buildTime", buildTime);
        
        return ResponseEntity.ok(simpleVersion);
    }
}
