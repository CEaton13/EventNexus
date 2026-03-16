package com.app.eventnexus.repositories;

import com.app.eventnexus.models.RefreshToken;
import com.app.eventnexus.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RefreshToken} entities.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token record by its token string.
     *
     * @param token the raw token value
     * @return an Optional containing the record, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens belonging to the given user.
     * Used during logout to invalidate all active sessions.
     *
     * @param user the user whose tokens should be deleted
     */
    @Transactional
    void deleteByUser(User user);
}
