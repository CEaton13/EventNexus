package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.TournamentStatus;

/**
 * Request DTO for updating a tournament's lifecycle status.
 * Used by {@code PATCH /api/tournaments/{id}/status}.
 */
public class TournamentStatusRequest {

    private TournamentStatus status;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }
}
