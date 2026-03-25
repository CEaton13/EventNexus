package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling a match.
 * Used by {@code PATCH /api/matches/{id}/schedule}.
 */
public class ScheduleMatchRequest {

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }
}
