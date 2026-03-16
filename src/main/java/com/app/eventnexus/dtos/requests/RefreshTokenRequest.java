package com.app.eventnexus.dtos.requests;

/**
 * Request body for {@code POST /api/auth/refresh}.
 */
public class RefreshTokenRequest {

    private String refreshToken;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
