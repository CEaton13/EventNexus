package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for registering a team in a tournament.
 * Used by {@code POST /api/tournaments/{id}/register}.
 */
public class RegistrationRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}