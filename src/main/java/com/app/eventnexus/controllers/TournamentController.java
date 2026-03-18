package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.RegistrationRequest;
import com.app.eventnexus.dtos.requests.RegistrationStatusRequest;
import com.app.eventnexus.dtos.requests.TournamentRequest;
import com.app.eventnexus.dtos.requests.TournamentStatusRequest;
import com.app.eventnexus.dtos.responses.BracketResponse;
import com.app.eventnexus.dtos.responses.RegistrationResponse;
import com.app.eventnexus.dtos.responses.TournamentResponse;
import com.app.eventnexus.dtos.responses.TournamentSummaryResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.BracketService;
import com.app.eventnexus.services.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller for tournament management.
 * Read endpoints are public; all write operations require {@code TOURNAMENT_ADMIN}.
 * All business logic and status-transition validation are delegated to
 * {@link TournamentService}.
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final BracketService bracketService;

    public TournamentController(TournamentService tournamentService,
                                BracketService bracketService) {
        this.tournamentService = tournamentService;
        this.bracketService = bracketService;
    }

    /**
     * Returns all tournaments as lightweight summary cards.
     * No authentication required.
     *
     * @return 200 OK with a list of tournament summaries
     */
    @GetMapping
    public ResponseEntity<List<TournamentSummaryResponse>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.findAll());
    }

    /**
     * Returns a single tournament with full detail including genre and venue.
     * No authentication required.
     *
     * @param id the tournament's primary key
     * @return 200 OK with full tournament detail, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.findById(id));
    }

    /**
     * Creates a new tournament in {@code DRAFT} status.
     * The authenticated admin automatically becomes the creator.
     *
     * @param request        tournament details
     * @param authentication the current user's security context
     * @return 201 Created with the new tournament
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<TournamentResponse> createTournament(@RequestBody TournamentRequest request,
                                                               Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.create(request, principal.getUserId()));
    }

    /**
     * Updates mutable fields of an existing tournament.
     * Does not change the tournament's lifecycle status — use {@code PATCH /{id}/status} for that.
     *
     * @param id      the tournament's primary key
     * @param request updated values
     * @return 200 OK with the updated tournament, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<TournamentResponse> updateTournament(@PathVariable Long id,
                                                               @RequestBody TournamentRequest request) {
        return ResponseEntity.ok(tournamentService.update(id, request));
    }

    /**
     * Deletes a tournament by its ID.
     *
     * @param id the tournament's primary key
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Advances a tournament's lifecycle status by one step.
     * Only the single valid next state is accepted; invalid transitions return 409.
     *
     * @param id      the tournament's primary key
     * @param request body containing the target {@link com.app.eventnexus.enums.TournamentStatus}
     * @return 200 OK with the updated tournament, or 409 on invalid transition
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<TournamentResponse> updateTournamentStatus(@PathVariable Long id,
                                                                     @RequestBody TournamentStatusRequest request) {
        return ResponseEntity.ok(tournamentService.updateStatus(id, request.getStatus()));
    }

    // ─── Bracket ──────────────────────────────────────────────────────────────

    /**
     * Returns the full bracket for a tournament, with matches grouped by round.
     * No authentication required.
     *
     * @param id the tournament's primary key
     * @return 200 OK with the bracket, or 404 if the tournament does not exist
     */
    @GetMapping("/{id}/bracket")
    public ResponseEntity<BracketResponse> getBracket(@PathVariable Long id) {
        return ResponseEntity.ok(bracketService.getBracket(id));
    }

    // ─── Team Registration ────────────────────────────────────────────────────

    /**
     * Registers a team for a tournament.
     * The tournament must be in {@code REGISTRATION_OPEN} status.
     * A {@code TEAM_MANAGER} may only register a team they manage; a
     * {@code TOURNAMENT_ADMIN} may register any team.
     *
     * @param id             the tournament's primary key
     * @param request        body containing the {@code teamId} to register
     * @param authentication the current user's security context
     * @return 201 Created with the registration entry, or 409 if already registered or tournament not open
     */
    @PostMapping("/{id}/register")
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER')")
    public ResponseEntity<RegistrationResponse> registerTeam(@PathVariable Long id,
                                                             @RequestBody RegistrationRequest request,
                                                             Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.registerTeam(id, request.getTeamId(),
                        principal.getUserId(), principal.getRole()));
    }

    /**
     * Returns all registered teams for a tournament with their registration status.
     * No authentication required.
     *
     * @param id the tournament's primary key
     * @return 200 OK with a list of registration entries
     */
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<RegistrationResponse>> getRegisteredTeams(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getRegisteredTeams(id));
    }

    /**
     * Updates a team's registration status (e.g. PENDING → APPROVED or REJECTED).
     * Only a {@code TOURNAMENT_ADMIN} may approve or reject registrations.
     *
     * @param id      the tournament's primary key
     * @param teamId  the team's primary key
     * @param request body containing the new {@link com.app.eventnexus.enums.RegistrationStatus}
     * @return 200 OK with the updated registration entry
     */
    @PatchMapping("/{id}/teams/{teamId}/status")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<RegistrationResponse> updateRegistrationStatus(@PathVariable Long id,
                                                                         @PathVariable Long teamId,
                                                                         @RequestBody RegistrationStatusRequest request) {
        return ResponseEntity.ok(tournamentService.updateRegistrationStatus(id, teamId, request.getStatus()));
    }
}
