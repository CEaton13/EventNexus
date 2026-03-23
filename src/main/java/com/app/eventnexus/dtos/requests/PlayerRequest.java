package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a player.
 * The team is specified via the URL path ({@code /api/teams/{teamId}/players}),
 * not in this body.
 */
public class PlayerRequest {

    @NotBlank(message = "Gamer tag is required")
    @Size(max = 50, message = "Gamer tag must not exceed 50 characters")
    private String gamerTag;

    @Size(max = 100, message = "Real name must not exceed 100 characters")
    private String realName;

    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public PlayerRequest() {
    }

    public PlayerRequest(String gamerTag, String realName, String position,
                         String country, String avatarUrl) {
        this.gamerTag = gamerTag;
        this.realName = realName;
        this.position = position;
        this.country = country;
        this.avatarUrl = avatarUrl;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
