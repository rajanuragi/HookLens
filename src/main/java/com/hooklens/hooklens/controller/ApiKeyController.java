
        package com.hooklens.hooklens.controller;

import com.hooklens.hooklens.dto.request.CreateApiKeyRequest;
import com.hooklens.hooklens.dto.response.ApiKeyCreatedResponse;
import com.hooklens.hooklens.dto.response.ApiKeyListResponse;
import com.hooklens.hooklens.service.ApiKeyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    // Create API key
    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse> createApiKey(
            @Valid @RequestBody CreateApiKeyRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        ApiKeyCreatedResponse response =
                apiKeyService.createApiKey(userId, request.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all API keys of logged-in user
    @GetMapping
    public ResponseEntity<List<ApiKeyListResponse>> getApiKeys(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<ApiKeyListResponse> response =
                apiKeyService.getApiKeys(userId);

        return ResponseEntity.ok(response);
    }

    // Revoke an API key
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        apiKeyService.revokeApiKey(userId, id);

        return ResponseEntity.noContent().build();
    }
}
