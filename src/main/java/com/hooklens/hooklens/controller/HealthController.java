
package com. hooklens. hooklens. controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> checkHealth() {

        return Map.of(
                "status", "UP",
                "application", "HookLens",
                "message", "HookLens API is running successfully"
        );
    }
}


