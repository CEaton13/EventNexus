package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.TeamFollow;

import java.time.LocalDateTime;

/**
 * Response DTO for a team-follow relationship.
 * Includes enough team info for the "Following" list without exposing the full entity graph.
 */
public class TeamFollowResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private String teamTag;
    private String logoUrl;
    private LocalDateTime createdAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code TeamFollowResponse} from a {@link TeamFollow} entity.
     *
     * @param follow the source entity (team must be loaded)
     * @return a populated response DTO
     */
    public static TeamFollowResponse from(TeamFollow follow) {
        TeamFollowResponse dto = new TeamFollowResponse();
        dto.id = follow.getId();
        dto.teamId = follow.getTeam().getId();
        dto.teamName = follow.getTeam().getName();
        dto.teamTag = follow.getTeam().getTag();
        dto.logoUrl = follow.getTeam().getLogoUrl();
        dto.createdAt = follow.getCreatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TeamFollowResponse() {}

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
