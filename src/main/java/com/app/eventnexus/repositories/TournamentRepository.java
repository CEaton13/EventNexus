package com.app.eventnexus.repositories;

import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Tournament} entities.
 */
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Returns all tournaments with the given status.
     *
     * @param status the lifecycle status to filter by
     * @return list of matching tournaments; never null
     */
    List<Tournament> findByStatus(TournamentStatus status);

    /**
     * Returns all tournaments associated with a specific game genre.
     *
     * @param genreId the game genre's primary key
     * @return list of tournaments for that genre; never null
     */
    List<Tournament> findByGameGenreId(Long genreId);
}