
package com.hooklens.hooklens.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EndpointListResponse(
        UUID id,
        String name,
        String tokenPrefix,
        LocalDateTime createdAt,
        LocalDateTime lastReceivedAt,
        LocalDateTime revokedAt
) {}
