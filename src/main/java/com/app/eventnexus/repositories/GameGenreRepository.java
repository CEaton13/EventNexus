package com.app.eventnexus.repositories;

import com.app.eventnexus.models.GameGenre;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link GameGenre} entities.
 */
public interface GameGenreRepository extends JpaRepository<GameGenre, Long> {

    /** Returns true if a genre with the given name exists (case-insensitive). */
    boolean existsByNameIgnoreCase(String name);
}
