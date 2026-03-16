package com.app.eventnexus.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT access token generation, validation, and claims extraction.
 * Refresh tokens are opaque UUID strings stored in the database; this class
 * only generates them — validation is done by querying the DB in AuthService.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    // ─── Token Generation ──────────────────────────────────────────────────────

    /**
     * Generates a signed JWT access token for the given user.
     * The subject claim is set to the username.
     *
     * @param userDetails the authenticated user
     * @return a compact, signed JWT string
     */
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates an opaque UUID refresh token string.
     * The caller is responsible for persisting this value in the database.
     *
     * @return a random UUID string
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // ─── Token Validation & Claims ─────────────────────────────────────────────

    /**
     * Validates a JWT access token — checks signature and expiry.
     *
     * @param token the JWT string to validate
     * @return true if the token is valid; false if expired, malformed, or tampered
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject claim) from a JWT access token.
     * Only call this after {@link #validateToken(String)} returns true.
     *
     * @param token a valid JWT string
     * @return the username encoded as the subject claim
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
