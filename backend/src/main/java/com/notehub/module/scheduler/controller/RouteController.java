package com.notehub.module.scheduler.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Lightweight dynamic route API.
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final ObjectMapper objectMapper;

    @Value("${scheduler.route-data-path:data/routes.json}")
    private String routeDataPath;

    public RouteController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Map<String, Object>> listRoutes() {
        try {
            File file = new File(routeDataPath);
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<>() {});
            }

            ClassPathResource fallback = new ClassPathResource("data/routes.json");
            try (InputStream inputStream = fallback.getInputStream()) {
                return objectMapper.readValue(inputStream, new TypeReference<>() {});
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load route config", e);
        }
    }
}
