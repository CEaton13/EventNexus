package com.app.eventnexus.exceptions;

/**
 * Thrown when an operation violates a uniqueness or business-rule constraint.
 * Examples: duplicate username, team already registered for a tournament,
 * venue station capacity exceeded, equipment already assigned.
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
