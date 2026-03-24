package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for recording a match result.
 * Used by {@code PATCH /api/matches/{id}/result}.
 */
public class ResultRequest {

    @NotNull(message = "Winner ID is required")
    private Long winnerId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }
}
