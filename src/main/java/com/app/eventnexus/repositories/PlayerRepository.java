package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Player} entities.
 */
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Returns all players belonging to a specific team, regardless of active status.
     *
     * @param teamId the team's primary key
     * @return list of players on that team
     */
    List<Player> findByTeam_Id(Long teamId);

    /**
     * Counts players with the given active status.
     * Pass {@code true} to count active players; {@code false} for soft-deleted ones.
     *
     * @param isActive the active flag to filter on
     * @return count of players with that status
     */
    Long countByIsActive(boolean isActive);

    /**
     * Finds a player by ID and active status.
     * Used to look up only active players, or explicitly fetch soft-deleted ones.
     *
     * @param id       the player's primary key
     * @param isActive the active status to filter on
     * @return an Optional containing the player if found with that status
     */
    Optional<Player> findByIdAndIsActive(Long id, boolean isActive);

    /**
     * Counts distinct active players whose team has participated in at least one
     * tournament belonging to the given organization.
     *
     * @param orgId the organization's primary key
     * @return count of distinct active players in the org's tournaments
     */
    @Query(value = """
            SELECT COUNT(DISTINCT p.id)
            FROM players p
            JOIN teams t             ON t.id = p.team_id
            JOIN tournament_teams tt ON tt.team_id = t.id
            JOIN tournaments tour    ON tour.id = tt.tournament_id
            WHERE tour.organization_id = :orgId
              AND p.is_active = true
            """, nativeQuery = true)
    Long countActiveByOrganizationId(@Param("orgId") Long orgId);
}
