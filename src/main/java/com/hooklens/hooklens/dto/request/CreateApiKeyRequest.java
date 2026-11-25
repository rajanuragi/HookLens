
package com.hooklens.hooklens.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(

        @NotBlank(message = "API key name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name

) {}

