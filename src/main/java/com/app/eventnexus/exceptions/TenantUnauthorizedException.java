package com.app.eventnexus.exceptions;

/**
 * Thrown when an authenticated user attempts to access an organization
 * they are not a member of, or when no valid tenant context can be
 * established for a request that requires one.
 *
 * <p>Mapped to HTTP 403 Forbidden by {@link GlobalExceptionHandler}.
 */
public class TenantUnauthorizedException extends RuntimeException {

    public TenantUnauthorizedException(String message) {
        super(message);
    }
}
