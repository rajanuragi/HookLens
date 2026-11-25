
package com.hooklens.hooklens.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EndpointCreatedResponse(
        UUID id,
        String name,
        String endpointToken,
        String tokenPrefix,
        String ingestionPath,
        LocalDateTime createdAt,
        String message
) {}
