package com.hooklens.hooklens.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebhookEventResponse(
        UUID id,
        UUID endpointId,
        String httpMethod,
        String requestHeaders,
        String queryParameters,
        String contentType,
        String parsedJson,
        LocalDateTime receivedAt
) {
}