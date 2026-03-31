package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.MyRegistrationResponse;
import com.app.eventnexus.repositories.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for retrieving all tournament registrations belonging to teams
 * managed by the authenticated user. Scoped to the user, not an org.
 */
@Service
public class MyRegistrationsService {

    private final TournamentTeamRepository tournamentTeamRepository;

    public MyRegistrationsService(TournamentTeamRepository tournamentTeamRepository) {
        this.tournamentTeamRepository = tournamentTeamRepository;
    }

    /**
     * Returns all tournament registrations for teams managed by the given user,
     * ordered by tournament start date descending. Returns an empty list for
     * users who manage no teams.
     *
     * @param managerId the authenticated user's ID
     * @return list of registration summaries; never null
     */
    @Transactional(readOnly = true)
    public List<MyRegistrationResponse> getRegistrationsForManager(Long managerId) {
        return tournamentTeamRepository.findByTeamManagerId(managerId)
                .stream()
                .map(MyRegistrationResponse::from)
                .toList();
    }
}
