package com.app.eventnexus.dtos.responses;

import java.util.List;

/**
 * Response DTO returned by login and token-refresh endpoints.
 * Contains the short-lived access token, the opaque refresh token, the
 * authenticated user's profile, and the user's organization memberships.
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponse user;
    private List<OrganizationMemberResponse> organizations;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, UserResponse user,
                        List<OrganizationMemberResponse> organizations) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.organizations = organizations;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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

    public List<OrganizationMemberResponse> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationMemberResponse> organizations) {
        this.organizations = organizations;
    }
}
