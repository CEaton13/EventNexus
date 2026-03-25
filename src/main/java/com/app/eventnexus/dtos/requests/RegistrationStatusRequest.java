package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a team's registration status within a tournament.
 * Used by {@code PATCH /api/tournaments/{id}/teams/{teamId}/status}.
 */
public class RegistrationStatusRequest {

    @NotNull(message = "Registration status is required")
    private RegistrationStatus status;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
