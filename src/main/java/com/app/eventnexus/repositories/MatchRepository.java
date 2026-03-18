package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Match} entities.
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Returns all matches belonging to a tournament, ordered by round then match number.
     *
     * @param tournamentId the tournament's primary key
     * @return list of matches; never null
     */
    List<Match> findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(Long tournamentId);

    /**
     * Returns all matches in a specific round of a tournament.
     *
     * @param tournamentId the tournament's primary key
     * @param roundNumber  the 1-based round number
     * @return list of matches for that round; never null
     */
    List<Match> findByTournamentIdAndRoundNumber(Long tournamentId, int roundNumber);
}
