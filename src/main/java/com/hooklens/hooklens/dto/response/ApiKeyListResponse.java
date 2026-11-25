
package com.hooklens.hooklens.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyListResponse(
        UUID id,
        String name,
        String keyPrefix,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        LocalDateTime revokedAt
) {}
