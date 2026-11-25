package com.hooklens.hooklens.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/developer")
public class DeveloperController {

    @GetMapping("/ping")
    public Map<String, Object> ping(Authentication authentication) {
        return Map.of(
                "message", "API key authentication successful",
                "apiKeyId", authentication.getName()
        );
    }
}
