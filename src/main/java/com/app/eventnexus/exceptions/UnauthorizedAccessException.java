package com.app.eventnexus.exceptions;

/**
 * Thrown when an authenticated user attempts an operation on a resource they
 * do not own (e.g. a TEAM_MANAGER editing another manager's team).
 * Maps to HTTP 403 Forbidden.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
