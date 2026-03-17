package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.Team;

import java.time.LocalDateTime;

/**
 * Response DTO for a team.
 * Exposes manager username (not the full user object) and the count of active
 * players so the caller has enough context without loading sub-collections.
 */
public class TeamResponse {

    private Long id;
    private String name;
    private String tag;
    private String logoUrl;
    private String homeRegion;
    private String managerUsername;
    private Long playerCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code TeamResponse} from a {@link Team} entity.
     * {@code playerCount} is not populated here — set it separately after
     * querying {@code TeamRepository.countActivePlayersByTeamId()}.
     *
     * @param team the source entity
     * @return a partially populated response DTO
     */
    public static TeamResponse from(Team team) {
        TeamResponse dto = new TeamResponse();
        dto.id = team.getId();
        dto.name = team.getName();
        dto.tag = team.getTag();
        dto.logoUrl = team.getLogoUrl();
        dto.homeRegion = team.getHomeRegion();
        dto.managerUsername = (team.getTeamManager() != null)
                ? team.getTeamManager().getUsername()
                : null;
        dto.createdAt = team.getCreatedAt();
        dto.updatedAt = team.getUpdatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TeamResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getHomeRegion() {
        return homeRegion;
    }

    public void setHomeRegion(String homeRegion) {
        this.homeRegion = homeRegion;
    }

    public String getManagerUsername() {
        return managerUsername;
    }

    public void setManagerUsername(String managerUsername) {
        this.managerUsername = managerUsername;
    }

    public Long getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Long playerCount) {
        this.playerCount = playerCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
