package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.LoginRequest;
import com.app.eventnexus.dtos.requests.RefreshTokenRequest;
import com.app.eventnexus.dtos.requests.RegisterRequest;
import com.app.eventnexus.dtos.responses.AuthResponse;
import com.app.eventnexus.dtos.responses.UserResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller for authentication operations.
 * All business logic is delegated to {@link AuthService}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     * The role defaults to {@code SPECTATOR} if not provided.
     *
     * @param request registration details
     * @return 201 Created with the new user's profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param request login credentials
     * @return 200 OK with access token, refresh token, and user profile
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @param request body containing the refresh token string
     * @return 200 OK with a fresh access token and the same refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Invalidates the authenticated user's refresh token, effectively logging out the session.
     * The caller must still discard their access token client-side.
     *
     * @param authentication the current user's security context
     * @return 204 No Content
     */
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER', 'SPECTATOR')")
    public ResponseEntity<Void> logout(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        authService.logout(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
