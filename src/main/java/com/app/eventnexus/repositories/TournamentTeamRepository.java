package com.app.eventnexus.repositories;

import com.app.eventnexus.models.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Finds a specific team's registration for a specific tournament.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @return an Optional containing the registration, or empty if not registered
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
}