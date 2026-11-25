
package com.hooklens.hooklens.controller;

import com.hooklens.hooklens.dto.request.CreateEndpointRequest;
import com.hooklens.hooklens.dto.response.EndpointCreatedResponse;
import com.hooklens.hooklens.dto.response.EndpointListResponse;
import com.hooklens.hooklens.service.WebhookEndpointService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/endpoints")
public class WebhookEndpointController {

    private final WebhookEndpointService endpointService;

    public WebhookEndpointController(
            WebhookEndpointService endpointService
    ) {
        this.endpointService = endpointService;
    }

    // Create endpoint
    @PostMapping
    public ResponseEntity<EndpointCreatedResponse> createEndpoint(
            @Valid @RequestBody CreateEndpointRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        EndpointCreatedResponse response =
                endpointService.createEndpoint(
                        userId,
                        request.name()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // List endpoints
    @GetMapping
    public ResponseEntity<List<EndpointListResponse>> getEndpoints(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<EndpointListResponse> response =
                endpointService.getEndpoints(userId);

        return ResponseEntity.ok(response);
    }

    // Delete endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        endpointService.deleteEndpoint(userId, id);

        return ResponseEntity.noContent().build();
    }
}
