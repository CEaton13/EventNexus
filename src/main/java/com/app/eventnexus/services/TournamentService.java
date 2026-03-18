package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.TournamentRequest;
import com.app.eventnexus.dtos.responses.RegistrationResponse;
import com.app.eventnexus.dtos.responses.TournamentResponse;
import com.app.eventnexus.dtos.responses.TournamentSummaryResponse;
import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.InvalidStateTransitionException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.UnauthorizedAccessException;
import com.app.eventnexus.models.GameGenre;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.TournamentTeam;
import com.app.eventnexus.models.User;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.GameGenreRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.TournamentTeamRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing tournament lifecycle and CRUD operations.
 *
 * <p>Status transition rules (any other transition throws
 * {@link InvalidStateTransitionException}):
 * <pre>
 *   DRAFT → REGISTRATION_OPEN
 *   REGISTRATION_OPEN → REGISTRATION_CLOSED
 *   REGISTRATION_CLOSED → IN_PROGRESS
 *   IN_PROGRESS → COMPLETED
 *   COMPLETED → ARCHIVED
 * </pre>
 */
@Service
public class TournamentService {

    private static final Map<TournamentStatus, TournamentStatus> VALID_TRANSITIONS = Map.of(
            TournamentStatus.DRAFT,                TournamentStatus.REGISTRATION_OPEN,
            TournamentStatus.REGISTRATION_OPEN,    TournamentStatus.REGISTRATION_CLOSED,
            TournamentStatus.REGISTRATION_CLOSED,  TournamentStatus.IN_PROGRESS,
            TournamentStatus.IN_PROGRESS,          TournamentStatus.COMPLETED,
            TournamentStatus.COMPLETED,            TournamentStatus.ARCHIVED
    );

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final GameGenreRepository gameGenreRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentTeamRepository tournamentTeamRepository,
                             GameGenreRepository gameGenreRepository,
                             VenueRepository venueRepository,
                             UserRepository userRepository,
                             TeamRepository teamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.gameGenreRepository = gameGenreRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all tournaments as lightweight summary DTOs.
     *
     * @return list of all tournaments; never null
     */
    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> findAll() {
        return tournamentRepository.findAll()
                .stream()
                .map(TournamentSummaryResponse::from)
                .toList();
    }

    /**
     * Returns a single tournament with full detail by its ID.
     *
     * @param id the tournament's primary key
     * @return the tournament as a full response DTO
     * @throws ResourceNotFoundException if no tournament exists with the given ID
     */
    @Transactional(readOnly = true)
    public TournamentResponse findById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", id));
        return TournamentResponse.from(tournament);
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new tournament in {@code DRAFT} status.
     * The tournament is automatically owned by the authenticated admin.
     *
     * @param request  tournament details
     * @param adminId  ID of the authenticated {@code TOURNAMENT_ADMIN} creating the tournament
     * @return the newly created tournament as a full response DTO
     * @throws ResourceNotFoundException if the referenced genre, venue, or user cannot be found
     */
    @Transactional
    public TournamentResponse create(TournamentRequest request, Long adminId) {
        GameGenre genre = gameGenreRepository.findById(request.getGameGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("GameGenre", request.getGameGenreId()));

        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", request.getVenueId()));
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        Tournament tournament = new Tournament(
                request.getName(),
                request.getDescription(),
                request.getGameTitle(),
                request.getFormat(),
                request.getMaxTeams(),
                request.getRegistrationStart(),
                request.getRegistrationEnd(),
                request.getStartDate(),
                request.getEndDate(),
                venue,
                genre,
                admin);

        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    /**
     * Updates mutable fields of an existing tournament.
     * Status changes must go through {@link #updateStatus(Long, TournamentStatus)} instead.
     *
     * @param id      the tournament's primary key
     * @param request updated values
     * @return the updated tournament as a full response DTO
     * @throws ResourceNotFoundException if the tournament, genre, or venue cannot be found
     */
    @Transactional
    public TournamentResponse update(Long id, TournamentRequest request) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", id));

        GameGenre genre = gameGenreRepository.findById(request.getGameGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("GameGenre", request.getGameGenreId()));

        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", request.getVenueId()));
        }

        tournament.setName(request.getName());
        tournament.setDescription(request.getDescription());
        tournament.setGameTitle(request.getGameTitle());
        tournament.setFormat(request.getFormat());
        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setRegistrationStart(request.getRegistrationStart());
        tournament.setRegistrationEnd(request.getRegistrationEnd());
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setVenue(venue);
        tournament.setGameGenre(genre);
        tournament.setUpdatedAt(LocalDateTime.now());

        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    /**
     * Deletes a tournament by its ID.
     *
     * @param id the tournament's primary key
     * @throws ResourceNotFoundException if no tournament exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", id));
        tournamentRepository.delete(tournament);
    }

    /**
     * Advances a tournament to the next lifecycle status.
     * Only the single valid next state for each current state is accepted;
     * any other target status throws {@link InvalidStateTransitionException}.
     *
     * @param tournamentId the tournament's primary key
     * @param newStatus    the desired target status
     * @return the updated tournament as a full response DTO
     * @throws ResourceNotFoundException       if no tournament exists with the given ID
     * @throws InvalidStateTransitionException if the transition from current → newStatus is not valid
     */
    @Transactional
    public TournamentResponse updateStatus(Long tournamentId, TournamentStatus newStatus) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));

        validateStatusTransition(tournament.getStatus(), newStatus);

        tournament.setStatus(newStatus);
        tournament.setUpdatedAt(LocalDateTime.now());

        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    // ─── Team Registration ─────────────────────────────────────────────────────

    /**
     * Registers a team for a tournament.
     * The tournament must be in {@code REGISTRATION_OPEN} status and the team must not
     * already be registered. A {@code TEAM_MANAGER} may only register their own team.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @param requesterId  ID of the authenticated user making the request
     * @param requesterRole role of the authenticated user
     * @return the new registration entry as a response DTO
     * @throws ResourceNotFoundException if the tournament or team cannot be found
     * @throws ConflictException         if the tournament is not open for registration or the team is already registered
     * @throws UnauthorizedAccessException if a {@code TEAM_MANAGER} attempts to register a team they do not manage
     */
    @Transactional
    public RegistrationResponse registerTeam(Long tournamentId, Long teamId,
                                             Long requesterId, UserRole requesterRole) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new ConflictException(
                    "Tournament is not open for registration. Current status: " + tournament.getStatus());
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        if (requesterRole == UserRole.TEAM_MANAGER) {
            Long managerId = team.getTeamManager() != null ? team.getTeamManager().getId() : null;
            if (!requesterId.equals(managerId)) {
                throw new UnauthorizedAccessException(
                        "You can only register a team that you manage.");
            }
        }

        tournamentTeamRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Team '" + team.getName() + "' is already registered for this tournament.");
                });

        TournamentTeam entry = new TournamentTeam(tournament, team);
        return RegistrationResponse.from(tournamentTeamRepository.save(entry));
    }

    /**
     * Updates the registration status of a team in a tournament.
     * Only a {@code TOURNAMENT_ADMIN} should call this (enforced at the controller layer).
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @param newStatus    the new registration status to apply
     * @return the updated registration entry as a response DTO
     * @throws ResourceNotFoundException if no registration exists for the given tournament + team pair
     */
    @Transactional
    public RegistrationResponse updateRegistrationStatus(Long tournamentId, Long teamId,
                                                         RegistrationStatus newStatus) {
        TournamentTeam entry = tournamentTeamRepository
                .findByTournamentIdAndTeamId(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament " + tournamentId + " and team " + teamId));

        entry.setRegistrationStatus(newStatus);
        return RegistrationResponse.from(tournamentTeamRepository.save(entry));
    }

    /**
     * Returns all team registrations for a given tournament.
     *
     * @param tournamentId the tournament's primary key
     * @return list of registration entries; never null
     * @throws ResourceNotFoundException if no tournament exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegisteredTeams(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament", tournamentId);
        }
        return tournamentTeamRepository.findByTournamentId(tournamentId)
                .stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    /**
     * Validates that {@code current → next} is a permitted status transition.
     *
     * @param current the tournament's current status
     * @param next    the desired target status
     * @throws InvalidStateTransitionException if the transition is not in the allowed set
     */
    private void validateStatusTransition(TournamentStatus current, TournamentStatus next) {
        TournamentStatus allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.equals(next)) {
            throw new InvalidStateTransitionException(
                    "Invalid status transition from " + current + " to " + next + ". "
                            + "Allowed transition from " + current + " is: "
                            + (allowed != null ? allowed : "none"));
        }
    }
}
