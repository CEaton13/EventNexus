package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.TournamentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a tournament's lifecycle status.
 * Used by {@code PATCH /api/tournaments/{id}/status}.
 */
public class TournamentStatusRequest {

    @NotNull(message = "Status is required")
    private TournamentStatus status;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }
}
