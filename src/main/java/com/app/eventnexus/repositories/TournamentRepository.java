package com.app.eventnexus.repositories;

import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Queries the {@code tournament_standings} view for a given tournament,
     * returning one row per approved team ordered by rank ascending.
     *
     * @param tournamentId the tournament's primary key
     * @return ordered list of standing rows as projection objects; never null
     */
    @Query(value = """
            SELECT team_id      AS teamId,
                   team_name    AS teamName,
                   team_tag     AS teamTag,
                   logo_url     AS logoUrl,
                   wins,
                   losses,
                   points,
                   rank
            FROM tournament_standings
            WHERE tournament_id = :tournamentId
            ORDER BY rank
            """, nativeQuery = true)
    List<StandingRow> findStandings(@Param("tournamentId") Long tournamentId);

    /**
     * Projection interface mapping one row of the {@code tournament_standings} view.
     */
    interface StandingRow {
        Long getTeamId();
        String getTeamName();
        String getTeamTag();
        String getLogoUrl();
        Long getWins();
        Long getLosses();
        Long getPoints();
        Long getRank();
    }
}