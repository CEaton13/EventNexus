package com.app.eventnexus.repositories;

import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.models.Tournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Note: findAll(Pageable) is inherited from JpaRepository — no declaration needed.

/**
 * Spring Data JPA repository for {@link Tournament} entities.
 */
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Finds a tournament by its exact name.
     * Used by {@code DataSeeder} to ensure idempotent tournament creation.
     *
     * @param name the tournament name
     * @return an Optional containing the tournament, or empty if not found
     */
    Optional<Tournament> findByName(String name);

    /**
     * Returns all tournaments with the given status.
     *
     * @param status the lifecycle status to filter by
     * @return list of matching tournaments; never null
     */
    List<Tournament> findByStatus(TournamentStatus status);

    /**
     * Returns a paginated list of tournaments filtered by status.
     */
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    /**
     * Returns a paginated list of tournaments filtered by game genre.
     */
    Page<Tournament> findByGameGenreId(Long gameGenreId, Pageable pageable);

    /**
     * Returns a paginated list of tournaments filtered by both status and game genre.
     */
    Page<Tournament> findByStatusAndGameGenreId(TournamentStatus status, Long gameGenreId, Pageable pageable);

    // ─── Org-scoped variants ───────────────────────────────────────────────────

    /**
     * Returns a paginated list of all tournaments belonging to a specific organization.
     */
    Page<Tournament> findByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Returns a paginated list of tournaments filtered by organization and status.
     */
    Page<Tournament> findByOrganizationIdAndStatus(Long organizationId, TournamentStatus status, Pageable pageable);

    /**
     * Returns a paginated list of tournaments filtered by organization and game genre.
     */
    Page<Tournament> findByOrganizationIdAndGameGenreId(Long organizationId, Long gameGenreId, Pageable pageable);

    /**
     * Returns a paginated list of tournaments filtered by organization, status, and game genre.
     */
    Page<Tournament> findByOrganizationIdAndStatusAndGameGenreId(Long organizationId, TournamentStatus status, Long gameGenreId, Pageable pageable);

    /**
     * Returns all tournaments associated with a specific game genre.
     *
     * @param genreId the game genre's primary key
     * @return list of tournaments for that genre; never null
     */
    List<Tournament> findByGameGenreId(Long genreId);

    /**
     * Flexible filtered query supporting all combinations of org scope, status,
     * genre, and start-date lower bound. {@code null} parameters are ignored.
     *
     * @param organizationId optional org scope; {@code null} = all orgs
     * @param status         optional status filter
     * @param genreId        optional genre filter
     * @param startAfter     optional lower bound (inclusive) on tournament start date
     * @param pageable       pagination and sort
     * @return matching page of tournaments
     */
    @Query("""
            SELECT t FROM Tournament t
            WHERE (:organizationId IS NULL OR t.organization.id = :organizationId)
              AND (:status        IS NULL OR t.status          = :status)
              AND (:genreId       IS NULL OR t.gameGenre.id    = :genreId)
              AND (:startAfter    IS NULL OR t.startDate       >= :startAfter)
            """)
    Page<Tournament> findAllFiltered(
            @Param("organizationId") Long organizationId,
            @Param("status")         TournamentStatus status,
            @Param("genreId")        Long genreId,
            @Param("startAfter")     LocalDateTime startAfter,
            Pageable pageable);

    /**
     * Counts tournaments in a given lifecycle status.
     * Used by the admin dashboard to build the status breakdown map.
     *
     * @param status the status to count
     * @return number of tournaments with that status
     */
    Long countByStatus(TournamentStatus status);

    /**
     * Counts tournaments for a specific organization in a given lifecycle status.
     * Used by the tenant-scoped admin dashboard.
     *
     * @param organizationId the organization's primary key
     * @param status         the status to count
     * @return number of tournaments with that status for the org
     */
    Long countByOrganizationIdAndStatus(Long organizationId, TournamentStatus status);

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