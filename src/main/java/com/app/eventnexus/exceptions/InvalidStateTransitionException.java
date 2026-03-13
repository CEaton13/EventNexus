package com.app.eventnexus.exceptions;

/**
 * Thrown when a status transition is attempted that is not permitted by the
 * defined lifecycle rules (e.g. DRAFT → IN_PROGRESS skipping intermediate states,
 * or COMPLETED → REGISTRATION_OPEN going backwards).
 * Maps to HTTP 409 Conflict.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String entityName, String fromState, String toState) {
        super("Invalid " + entityName + " status transition: " + fromState + " → " + toState);
    }
}
