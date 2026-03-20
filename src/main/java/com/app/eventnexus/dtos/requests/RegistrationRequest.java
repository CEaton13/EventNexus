package com.app.eventnexus.dtos.requests;

/**
 * Request DTO for registering a team in a tournament.
 * Used by {@code POST /api/tournaments/{id}/register}.
 */
public class RegistrationRequest {

    private Long teamId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}