package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
