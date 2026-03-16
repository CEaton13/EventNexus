package com.app.eventnexus.repositories;

import com.app.eventnexus.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username (case-sensitive).
     *
     * @param username the username to look up
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to look up
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns true if a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if taken, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Returns true if a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if taken, false otherwise
     */
    boolean existsByEmail(String email);
}
