
package com.hooklens.hooklens.service;

import com.hooklens.hooklens.dto.response.ApiKeyCreatedResponse;
import com.hooklens.hooklens.dto.response.ApiKeyListResponse;
import com.hooklens.hooklens.entity.ApiKey;
import com.hooklens.hooklens.entity.User;
import com.hooklens.hooklens.repository.ApiKeyRepository;
import com.hooklens.hooklens.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiKeyCreatedResponse createApiKey(
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

        String secret = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(secretBytes);

        String originalKey = "hl_live_" + prefix + "_" + secret;

        ApiKey apiKey = new ApiKey();

        apiKey.setUser(user);
        apiKey.setName(name.trim());
        apiKey.setKeyPrefix(prefix);
        apiKey.setKeyHash(passwordEncoder.encode(originalKey));

        ApiKey savedKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreatedResponse(
                savedKey.getId(),
                savedKey.getName(),
                originalKey,
                savedKey.getKeyPrefix(),
                savedKey.getCreatedAt(),
                "API key created successfully. Save it securely; it will not be shown again."
        );
    }

    @Transactional(readOnly = true)
    public List<ApiKeyListResponse> getApiKeys(UUID userId) {

        return apiKeyRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(key -> new ApiKeyListResponse(
                        key.getId(),
                        key.getName(),
                        key.getKeyPrefix(),
                        key.getCreatedAt(),
                        key.getLastUsedAt(),
                        key.getRevokedAt()
                ))
                .toList();
    }

    @Transactional
    public void revokeApiKey(UUID userId, UUID keyId) {

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getUser().getId().equals(userId))
                .orElseThrow(() ->
                        new IllegalArgumentException("API key not found")
                );

        if (apiKey.getRevokedAt() != null) {
            throw new IllegalStateException("API key is already revoked");
        }

        apiKey.setRevokedAt(LocalDateTime.now());
    }
}
