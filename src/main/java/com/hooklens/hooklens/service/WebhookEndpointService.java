
package com.hooklens.hooklens.service;

import com.hooklens.hooklens.dto.response.EndpointCreatedResponse;
import com.hooklens.hooklens.dto.response.EndpointListResponse;
import com.hooklens.hooklens.entity.User;
import com.hooklens.hooklens.entity.WebhookEndpoint;
import com.hooklens.hooklens.repository.UserRepository;
import com.hooklens.hooklens.repository.WebhookEndpointRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class WebhookEndpointService {

    private final WebhookEndpointRepository endpointRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookEndpointService(
            WebhookEndpointRepository endpointRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.endpointRepository = endpointRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create a new webhook endpoint
    @Transactional
    public EndpointCreatedResponse createEndpoint(
            UUID userId,
            String name
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        byte[] prefixBytes = new byte[6];
        byte[] secretBytes = new byte[32];

        secureRandom.nextBytes(prefixBytes);
        secureRandom.nextBytes(secretBytes);

        String prefix = HexFormat.of().formatHex(prefixBytes);

        String secret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(secretBytes);

        String originalToken = "hl_ep_" + prefix + "_" + secret;

        WebhookEndpoint endpoint = new WebhookEndpoint();

        endpoint.setUser(user);
        endpoint.setName(name.trim());
        endpoint.setTokenPrefix(prefix);
        endpoint.setTokenHash(
                passwordEncoder.encode(originalToken)
        );

        WebhookEndpoint savedEndpoint =
                endpointRepository.save(endpoint);

        return new EndpointCreatedResponse(
                savedEndpoint.getId(),
                savedEndpoint.getName(),
                originalToken,
                savedEndpoint.getTokenPrefix(),
                "/e/" + originalToken,
                savedEndpoint.getCreatedAt(),
                "Endpoint created successfully. Save the token securely; it will not be shown again."
        );
    }

    // Get all endpoints belonging to a user
    @Transactional(readOnly = true)
    public List<EndpointListResponse> getEndpoints(UUID userId) {

        return endpointRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(endpoint -> new EndpointListResponse(
                        endpoint.getId(),
                        endpoint.getName(),
                        endpoint.getTokenPrefix(),
                        endpoint.getCreatedAt(),
                        endpoint.getLastReceivedAt(),
                        endpoint.getRevokedAt()
                ))
                .toList();
    }

    // Delete endpoint using soft deletion
    @Transactional
    public void deleteEndpoint(UUID userId, UUID endpointId) {

        WebhookEndpoint endpoint = endpointRepository
                .findByIdAndUser_Id(endpointId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Endpoint not found")
                );

        if (endpoint.getRevokedAt() != null) {
            throw new IllegalStateException(
                    "Endpoint is already deleted"
            );
        }

        endpoint.setRevokedAt(LocalDateTime.now());
    }
}
