package com.abalone.controller;

import com.abalone.config.RequestLoggingInterceptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final RequestLoggingInterceptor interceptor;

    public LogController(RequestLoggingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLogs() {
        return ResponseEntity.ok(Map.of(
                "total", interceptor.getCount(),
                "logs", interceptor.getLogs()
        ));
    }
}
