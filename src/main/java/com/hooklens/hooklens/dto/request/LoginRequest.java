
package com.hooklens.hooklens.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        String password

) {}
