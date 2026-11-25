
package com.hooklens.hooklens.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEndpointRequest(

        @NotBlank(message = "Endpoint name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name

) {}
