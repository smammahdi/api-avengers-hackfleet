package com.careforall.auth.service;

import com.careforall.auth.dto.*;
import com.careforall.auth.entity.User;
import com.careforall.auth.event.UserRegisteredEvent;
import com.careforall.auth.repository.UserRepository;
import com.careforall.auth.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RabbitTemplate rabbitTemplate;

    private static final String USER_EXCHANGE = "user.exchange";
    private static final String USER_REGISTERED_ROUTING_KEY = "user.registered";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(User.UserRole.DONOR)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Publish UserRegisteredEvent to trigger guest donation linking
        publishUserRegisteredEvent(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        try {
            String token = request.getToken();

            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            log.debug("Token validated successfully for user: {}", email);

            return ValidateTokenResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .message("Token is valid")
                    .build();

        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return new ValidateTokenResponse(false, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            return new ValidateTokenResponse(false, "Token validation failed");
        }
    }

    public UserResponse getCurrentUser(String token) {
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .active(user.getActive())
                    .createdAt(user.getCreatedAt())
                    .build();

        } catch (JwtException e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Publish UserRegisteredEvent to RabbitMQ
     * This triggers guest donation linking in donation-service
     */
    private void publishUserRegisteredEvent(User user) {
        try {
            UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getName(),
                LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_REGISTERED_ROUTING_KEY, event);
            log.info("Published UserRegisteredEvent for user: {} (ID: {})", user.getEmail(), user.getId());
        } catch (Exception e) {
            log.error("Failed to publish UserRegisteredEvent for user {}: {}", user.getEmail(), e.getMessage());
            // Don't fail registration if event publishing fails
        }
    }
}
