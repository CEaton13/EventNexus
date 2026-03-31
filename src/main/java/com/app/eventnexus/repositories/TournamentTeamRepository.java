package com.app.eventnexus.repositories;

import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.models.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TournamentTeam} entities.
 */
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

    /**
     * Returns all team registrations for a given tournament.
     *
     * @param tournamentId the tournament's primary key
     * @return list of registrations; never null
     */
    List<TournamentTeam> findByTournamentId(Long tournamentId);

    /**
     * Returns all tournament registrations for a given team, eagerly fetching
     * the tournament and its game genre to avoid lazy-load issues.
     *
     * @param teamId the team's primary key
     * @return list of tournament registrations; never null
     */
    @Query("SELECT tt FROM TournamentTeam tt " +
           "JOIN FETCH tt.tournament t " +
           "JOIN FETCH t.gameGenre " +
           "WHERE tt.team.id = :teamId")
    List<TournamentTeam> findByTeam_Id(@Param("teamId") Long teamId);

    /**
     * Finds a specific team's registration for a specific tournament.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @return an Optional containing the registration, or empty if not registered
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    /**
     * Returns all tournament registrations for teams managed by a specific user.
     * Eagerly fetches tournament, team, and game genre to avoid N+1 queries.
     * Used by the "My Registrations" endpoint.
     *
     * @param managerId the team manager's user ID
     * @return list of registrations across all managed teams; empty if none
     */
    @Query("SELECT tt FROM TournamentTeam tt " +
           "JOIN FETCH tt.tournament t " +
           "JOIN FETCH t.gameGenre " +
           "JOIN FETCH tt.team tm " +
           "WHERE tm.teamManager.id = :managerId " +
           "ORDER BY t.startDate DESC")
    List<TournamentTeam> findByTeamManagerId(@Param("managerId") Long managerId);

    /**
     * Returns all APPROVED registrations for a tournament ordered by seed ascending,
     * with null seeds placed last.
     * Used by {@code BracketService} to determine round-1 team seeding.
     *
     * @param tournamentId     the tournament's primary key
     * @param status           the registration status to filter on (pass {@code APPROVED})
     * @return ordered list of approved registrations; never null
     */
    @Query("SELECT tt FROM TournamentTeam tt " +
           "WHERE tt.tournament.id = :tournamentId " +
           "AND tt.registrationStatus = :status " +
           "ORDER BY CASE WHEN tt.seed IS NULL THEN 1 ELSE 0 END ASC, tt.seed ASC")
    List<TournamentTeam> findApprovedTeamsOrderedBySeed(
            @Param("tournamentId") Long tournamentId,
            @Param("status") RegistrationStatus status);
}