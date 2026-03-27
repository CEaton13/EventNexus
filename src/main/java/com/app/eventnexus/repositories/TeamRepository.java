package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Team} entities.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Returns all teams managed by the specified user.
     *
     * @param managerId the user ID of the team manager
     * @return list of teams owned by that manager
     */
    List<Team> findByTeamManager_Id(Long managerId);

    /**
     * Finds a team by its exact name.
     * Used by {@code DataSeeder} to ensure idempotent team creation.
     *
     * @param name the team's display name
     * @return an Optional containing the team, or empty if not found
     */
    Optional<Team> findByName(String name);

    /**
     * Counts the number of active (non-soft-deleted) players on a team.
     * Uses a native query against the {@code players} table so this works
     * before the Player JPA entity is available.
     *
     * @param teamId the team's primary key
     * @return count of active players on the team
     */
    @Query(value = "SELECT COUNT(*) FROM players WHERE team_id = :teamId AND is_active = true",
           nativeQuery = true)
    Long countActivePlayersByTeamId(@Param("teamId") Long teamId);

    /**
     * Counts distinct teams that have participated in at least one tournament
     * belonging to the given organization.
     *
     * @param orgId the organization's primary key
     * @return count of distinct teams registered in any of the org's tournaments
     */
    @Query(value = """
            SELECT COUNT(DISTINCT t.id)
            FROM teams t
            JOIN tournament_teams tt ON tt.team_id = t.id
            JOIN tournaments tour    ON tour.id = tt.tournament_id
            WHERE tour.organization_id = :orgId
            """, nativeQuery = true)
    Long countByOrganizationId(@Param("orgId") Long orgId);

    /**
     * Returns the IDs of distinct teams registered in any tournament belonging
     * to the given organization. Used for org-scoped team listing.
     *
     * @param orgId the organization's primary key
     * @return list of distinct team IDs
     */
    @Query(value = """
            SELECT DISTINCT t.id
            FROM teams t
            JOIN tournament_teams tt ON tt.team_id = t.id
            JOIN tournaments tour    ON tour.id = tt.tournament_id
            WHERE tour.organization_id = :orgId
            """, nativeQuery = true)
    List<Long> findIdsByOrganizationId(@Param("orgId") Long orgId);

    /**
     * Returns all teams whose IDs are in the given collection.
     * Used together with {@link #findIdsByOrganizationId} for org-scoped paging.
     */
    List<Team> findByIdIn(Collection<Long> ids);
}
