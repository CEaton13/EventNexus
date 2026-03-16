package com.app.eventnexus.dtos.responses;

/**
 * Response DTO returned by login, register, and token-refresh endpoints.
 * Contains the short-lived access token and the authenticated user's profile.
 * The refresh token is transmitted via an httpOnly cookie, not in this body.
 */
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, UserResponse user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
