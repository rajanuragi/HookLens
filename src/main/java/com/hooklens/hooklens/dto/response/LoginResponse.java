
package com.hooklens.hooklens.dto.response;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String name,
        String email,
        String accessToken,
        String tokenType,
        long expiresIn
) {}
