package com.app.eventnexus.repositories;

import com.app.eventnexus.models.EquipmentLoadout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link EquipmentLoadout} entities.
 */
public interface EquipmentLoadoutRepository extends JpaRepository<EquipmentLoadout, Long> {

    /**
     * Returns all equipment loadouts for a given tournament.
     *
     * @param tournamentId the tournament's primary key
     * @return list of loadouts; never null
     */
    List<EquipmentLoadout> findByTournamentId(Long tournamentId);

    /**
     * Returns all equipment loadouts for a specific team in a specific tournament.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @return list of loadouts; never null
     */
    List<EquipmentLoadout> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    /**
     * Checks whether an equipment item has an active (not returned) loadout
     * for a given tournament — enforces the unique-per-tournament business rule
     * at the application level before the DB constraint fires.
     *
     * @param equipmentId  the equipment's primary key
     * @param tournamentId the tournament's primary key
     * @return an Optional containing the existing loadout, or empty if none
     */
    @Query("SELECT el FROM EquipmentLoadout el " +
           "WHERE el.equipment.id = :equipmentId " +
           "AND el.tournament.id = :tournamentId " +
           "AND el.returnedAt IS NULL")
    Optional<EquipmentLoadout> findActiveByEquipmentIdAndTournamentId(
            @Param("equipmentId") Long equipmentId,
            @Param("tournamentId") Long tournamentId);

    /**
     * Counts active (not-yet-returned) loadouts for a piece of equipment.
     * Used in {@code EquipmentService.delete()} to block deletion while
     * the equipment is still assigned.
     *
     * @param equipmentId the equipment's primary key
     * @return count of active loadouts
     */
    @Query("SELECT COUNT(el) FROM EquipmentLoadout el " +
           "WHERE el.equipment.id = :equipmentId " +
           "AND el.returnedAt IS NULL")
    long countActiveByEquipmentId(@Param("equipmentId") Long equipmentId);
}
