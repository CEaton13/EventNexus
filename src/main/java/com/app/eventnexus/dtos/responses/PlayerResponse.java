package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.Player;

import java.time.LocalDateTime;

/**
 * Response DTO for a player profile.
 * Soft-deleted players are still returned with {@code active = false} so that
 * historical records remain visible.
 */
public class PlayerResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private String gamerTag;
    private String realName;
    private String position;
    private String country;
    private String avatarUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code PlayerResponse} from a {@link Player} entity.
     *
     * @param player the source entity
     * @return a populated response DTO
     */
    public static PlayerResponse from(Player player) {
        PlayerResponse dto = new PlayerResponse();
        dto.id = player.getId();
        dto.teamId = player.getTeam().getId();
        dto.teamName = player.getTeam().getName();
        dto.gamerTag = player.getGamerTag();
        dto.realName = player.getRealName();
        dto.position = player.getPosition();
        dto.country = player.getCountry();
        dto.avatarUrl = player.getAvatarUrl();
        dto.active = player.isActive();
        dto.createdAt = player.getCreatedAt();
        dto.updatedAt = player.getUpdatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public PlayerResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
