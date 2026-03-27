package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for recording a match result.
 * Used by {@code PATCH /api/matches/{id}/result}.
 */
public class ResultRequest {

    @NotNull(message = "Winner ID is required")
    private Long winnerId;

    /** Optional final score for the winning team's side. */
    private Integer scoreA;

    /** Optional final score for the losing team's side. */
    private Integer scoreB;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getScoreA() {
        return scoreA;
    }

    public void setScoreA(Integer scoreA) {
        this.scoreA = scoreA;
    }

    public Integer getScoreB() {
        return scoreB;
    }

    public void setScoreB(Integer scoreB) {
        this.scoreB = scoreB;
    }
}
