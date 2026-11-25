
package com.hooklens.hooklens.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;
import com.hooklens.hooklens.dto.request.LoginRequest;
import com.hooklens.hooklens.dto.response.LoginResponse;
import com.hooklens.hooklens.dto.request.RegisterRequest;
import com.hooklens.hooklens.dto.response.UserResponse;
import com.hooklens.hooklens.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request
    ) {

        UserResponse response = userService.registerUser(
                request.name(),
                request.email(),
                request.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.loginUser(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")

    public ResponseEntity<Map<String, String>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                Map.of(
                        "userId", jwt.getSubject(),
                        "message", "You are authenticated successfully"
                )
        );
    }


}
