package com.iitp.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthCheckController {
    @Value("${server.port}")
    private String serverPort;

    @GetMapping()
    public String check() {
        return "✅ server listening on port " + serverPort;
    }
}