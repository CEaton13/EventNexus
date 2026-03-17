package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
