package com.lz.devflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class HealthCheckController {

    @GetMapping()
    public String ping() {
        return "pong";
    }
}
