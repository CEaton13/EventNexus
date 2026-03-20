package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.RegistrationStatus;

/**
 * Request DTO for updating a team's registration status within a tournament.
 * Used by {@code PATCH /api/tournaments/{id}/teams/{teamId}/status}.
 */
public class RegistrationStatusRequest {

    private RegistrationStatus status;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
