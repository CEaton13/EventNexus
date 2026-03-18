package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    /**
     * Counts matches that would create a team scheduling conflict.
     * A conflict exists when either team is already in a match whose window
     * {@code [scheduled_time, scheduled_time + 2h]} overlaps the proposed window.
     *
     * @param matchId       the match being scheduled (excluded from the count)
     * @param teamAId       primary key of team A
     * @param teamBId       primary key of team B
     * @param scheduledTime proposed start time
     * @param endTime       proposed end time (scheduledTime + 2 hours)
     * @return count of overlapping matches for either team
     */
    @Query(value = """
            SELECT COUNT(*) FROM matches
            WHERE id != :matchId
              AND status NOT IN ('BYE', 'COMPLETED')
              AND (team_a_id = :teamAId OR team_a_id = :teamBId
                   OR team_b_id = :teamAId OR team_b_id = :teamBId)
              AND scheduled_time IS NOT NULL
              AND scheduled_time < :endTime
              AND scheduled_time + INTERVAL '2 hours' > :scheduledTime
            """, nativeQuery = true)
    Long countTeamConflicts(@Param("matchId") Long matchId,
                            @Param("teamAId") Long teamAId,
                            @Param("teamBId") Long teamBId,
                            @Param("scheduledTime") LocalDateTime scheduledTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * Counts concurrent matches at a venue that would exceed its station capacity.
     * A conflict exists when the venue already has {@code >= station_count} matches
     * whose window overlaps the proposed window.
     *
     * @param matchId       the match being scheduled (excluded from the count)
     * @param venueId       the venue's primary key
     * @param scheduledTime proposed start time
     * @param endTime       proposed end time (scheduledTime + 2 hours)
     * @return count of overlapping matches at the venue
     */
    @Query(value = """
            SELECT COUNT(*) FROM matches
            WHERE id != :matchId
              AND venue_id = :venueId
              AND status NOT IN ('BYE', 'COMPLETED')
              AND scheduled_time IS NOT NULL
              AND scheduled_time < :endTime
              AND scheduled_time + INTERVAL '2 hours' > :scheduledTime
            """, nativeQuery = true)
    Long countVenueConflicts(@Param("matchId") Long matchId,
                             @Param("venueId") Long venueId,
                             @Param("scheduledTime") LocalDateTime scheduledTime,
                             @Param("endTime") LocalDateTime endTime);
}
