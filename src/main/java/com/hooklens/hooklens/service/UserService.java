
package com.hooklens.hooklens.service;

import com.hooklens.hooklens.service.JwtService;
import com.hooklens.hooklens.dto.response.LoginResponse;
import com.hooklens.hooklens.exception.InvalidCredentialsException;
import com.hooklens.hooklens.dto.response.UserResponse;
import com.hooklens.hooklens.entity.User;
import com.hooklens.hooklens.exception.EmailAlreadyExistsException;
import com.hooklens.hooklens.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }



    @Transactional(readOnly = true)
    public LoginResponse loginUser(String email, String rawPassword) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password."
                ));

        if (!passwordEncoder.matches(
                rawPassword,
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        String accessToken = jwtService.generateToken(user.getId());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                accessToken,
                "Bearer",
                900
        );
    }


    @Transactional
    public UserResponse registerUser(
            String name,
            String email,
            String rawPassword
    ) {

        // 1. Normalize user input
        String normalizedName = name.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        // 2. Check whether email already exists
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        // 3. Create a new User entity
        User user = new User();

        user.setName(normalizedName);
        user.setEmail(normalizedEmail);

        // 4. Hash the password using BCrypt
        String hashedPassword = passwordEncoder.encode(rawPassword);

        user.setPasswordHash(hashedPassword);

        // 5. Save user in PostgreSQL
        User savedUser = userRepository.save(user);

        // 6. Return a safe response DTO
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
}
