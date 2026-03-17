package com.app.eventnexus.dtos.requests;

/**
 * Request body for creating or updating a player.
 * The team is specified via the URL path ({@code /api/teams/{teamId}/players}),
 * not in this body.
 */
public class PlayerRequest {

    private String gamerTag;
    private String realName;
    private String position;
    private String country;
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
