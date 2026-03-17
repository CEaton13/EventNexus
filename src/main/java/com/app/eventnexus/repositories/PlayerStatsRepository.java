package com.app.eventnexus.repositories;

import com.app.eventnexus.models.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PlayerStats} entities.
 */
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {

    /**
     * Finds a player's stats record for a specific tournament.
     * Used for upsert logic in {@code PlayerService.recordStats()}.
     *
     * @param playerId     the player's primary key
     * @param tournamentId the tournament's primary key
     * @return an Optional containing the stats if they exist
     */
    Optional<PlayerStats> findByPlayer_IdAndTournamentId(Long playerId, Long tournamentId);

    /**
     * Returns all stats for a player across every tournament they have participated in,
     * joined with the tournament name for display purposes.
     * Uses a native query since the {@code Tournament} entity is not yet available.
     *
     * @param playerId the player's primary key
     * @return list of stats projections enriched with tournament name
     */
    @Query(value = """
            SELECT ps.id            AS id,
                   ps.tournament_id AS tournamentId,
                   t.name           AS tournamentName,
                   ps.wins          AS wins,
                   ps.losses        AS losses,
                   ps.mvp_count     AS mvpCount
            FROM player_stats ps
            JOIN tournaments t ON t.id = ps.tournament_id
            WHERE ps.player_id = :playerId
            ORDER BY t.start_date DESC NULLS LAST
            """, nativeQuery = true)
    List<StatsWithTournament> findStatsWithTournamentByPlayerId(@Param("playerId") Long playerId);

    /**
     * Projection interface for the native stats query that joins with
     * the {@code tournaments} table to include the tournament name.
     */
    interface StatsWithTournament {
        Long getId();
        Long getTournamentId();
        String getTournamentName();
        Integer getWins();
        Integer getLosses();
        Integer getMvpCount();
    }
}
