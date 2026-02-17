package com.abalone.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final int MAX_LOGS = 200;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final ConcurrentLinkedDeque<Map<String, Object>> logs = new ConcurrentLinkedDeque<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String uri = request.getRequestURI();

        // Ne pas logger les requêtes de logs elles-mêmes ni les fichiers statiques
        if (uri.equals("/api/logs") || !uri.startsWith("/api/")) {
            return;
        }

        long startTime = (long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> log = Map.of(
                "timestamp", LocalDateTime.now().format(FORMATTER),
                "method", request.getMethod(),
                "url", uri,
                "status", response.getStatus(),
                "duration", duration
        );

        logs.addLast(log);

        // Garder seulement les N derniers logs
        while (logs.size() > MAX_LOGS) {
            logs.pollFirst();
        }
    }

    public List<Map<String, Object>> getLogs() {
        return new ArrayList<>(logs);
    }

    public int getCount() {
        return logs.size();
    }
}
