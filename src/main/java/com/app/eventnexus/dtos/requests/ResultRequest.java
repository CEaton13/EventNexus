package com.app.eventnexus.dtos.requests;

/**
 * Request DTO for recording a match result.
 * Used by {@code PATCH /api/matches/{id}/result}.
 */
public class ResultRequest {

    private Long winnerId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }
}
