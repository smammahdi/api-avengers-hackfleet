package com.careforall.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 *
 * This filter validates JWT tokens and adds user context headers (X-User-Id, X-User-Role, X-User-Email)
 * to all downstream service requests. This eliminates the need for individual services to validate JWT.
 *
 * Flow:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token signature and expiration
 * 3. Extract user claims (userId, role, email)
 * 4. Add X-User-Id, X-User-Role, X-User-Email headers to the request
 * 5. Forward to downstream service
 *
 * Public endpoints (login, register) bypass this filter.
 *
 * @author API Avengers Team
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/auth/register",
        "/api/auth/login",
        "/api/campaigns",
        "/api/donations",
        "/actuator/health",
        "/actuator/prometheus"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // Check if the endpoint is public
            if (isPublicEndpoint(path)) {
                logger.debug("Public endpoint accessed: {}", path);
                return chain.filter(exchange);
            }

            // Extract Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format for path: {}", path);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate JWT and extract claims
                Claims claims = validateToken(token);

                // Extract user information from claims
                String userId = claims.get("userId", String.class);
                String role = claims.get("role", String.class);
                String email = claims.getSubject();

                logger.info("JWT validated successfully for user: {} (role: {})", email, role);

                // Add user context headers to the request
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Email", email)
                    .build();

                // Continue with the modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Validates the JWT token and extracts claims
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Checks if the requested path is a public endpoint
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Handles error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorBody = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
