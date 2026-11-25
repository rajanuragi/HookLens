
package com.hooklens.hooklens.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCreatedResponse(
        UUID id,
        String name,
        String apiKey,
        String keyPrefix,
        LocalDateTime createdAt,
        String message
) {}
