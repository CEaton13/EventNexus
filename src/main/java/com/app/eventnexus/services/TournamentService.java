package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.TournamentRequest;
import com.app.eventnexus.dtos.responses.TournamentResponse;
import com.app.eventnexus.dtos.responses.TournamentSummaryResponse;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.exceptions.InvalidStateTransitionException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.GameGenre;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.User;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.GameGenreRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final GameGenreRepository gameGenreRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             GameGenreRepository gameGenreRepository,
                             VenueRepository venueRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.gameGenreRepository = gameGenreRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
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
