package com.app.eventnexus.enums;

/**
 * Lifecycle states for a single match.
 * Valid transitions: UNSCHEDULED → SCHEDULED → IN_PROGRESS → COMPLETED
 * BYE is a terminal state set during bracket generation for uncontested slots.
 */
public enum MatchStatus {
    UNSCHEDULED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    BYE
}
