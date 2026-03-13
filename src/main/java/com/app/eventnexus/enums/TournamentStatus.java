package com.app.eventnexus.enums;

/**
 * Lifecycle states for a tournament.
 * Valid transitions: DRAFT → REGISTRATION_OPEN → REGISTRATION_CLOSED → IN_PROGRESS → COMPLETED → ARCHIVED
 */
public enum TournamentStatus {
    DRAFT,
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}
