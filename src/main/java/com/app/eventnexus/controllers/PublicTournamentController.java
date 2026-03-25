package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.BracketResponse;
import com.app.eventnexus.dtos.responses.PageResponse;
import com.app.eventnexus.dtos.responses.RegistrationResponse;
import com.app.eventnexus.dtos.responses.StandingsResponse;
import com.app.eventnexus.dtos.responses.TournamentResponse;
import com.app.eventnexus.dtos.responses.TournamentSummaryResponse;
import com.app.eventnexus.services.BracketService;
import com.app.eventnexus.services.TournamentService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public read-only tournament endpoints accessible without authentication
 * and without an org-slug context.
 *
 * <p>These endpoints power the public tournament hub at {@code /t/:id}.
 * They reuse the same service methods as the org-scoped controller but
 * are declared at {@code /api/tournaments/**} and are permit-all in
 * {@code SecurityConfig}.</p>
 *
 * <p>No write operations are exposed here. All mutation endpoints remain
 * behind authentication in {@link TournamentController}.</p>
 */
@RestController
@RequestMapping("/api/tournaments")
public class PublicTournamentController {

    private final TournamentService tournamentService;
    private final BracketService bracketService;

    public PublicTournamentController(TournamentService tournamentService,
                                      BracketService bracketService) {
        this.tournamentService = tournamentService;
        this.bracketService = bracketService;
    }

    /**
     * Returns a paginated list of all tournaments across all organizations.
     * Used by the public tournament browser — no authentication required.
     * Supports {@code ?page=0&size=20&sort=startDate,desc} query parameters.
     *
     * @param pageable pagination and sort parameters (default: 20 per page, newest first)
     * @return 200 OK with a page of tournament summaries
     */
    @GetMapping
    public ResponseEntity<PageResponse<TournamentSummaryResponse>> getAllTournaments(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(tournamentService.findAll(pageable)));
    }

    /**
     * Returns a single tournament by ID.
     *
     * @param id the tournament primary key
     * @return the tournament details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.findById(id));
    }

    /**
     * Returns the bracket structure for a tournament.
     *
     * @param id the tournament primary key
     * @return the bracket with all rounds and match data
     */
    @GetMapping("/{id}/bracket")
    public ResponseEntity<BracketResponse> getBracket(@PathVariable Long id) {
        return ResponseEntity.ok(bracketService.getBracket(id));
    }

    /**
     * Returns current standings for a tournament.
     *
     * @param id the tournament primary key
     * @return list of standing rows ordered by rank
     */
    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingsResponse>> getStandings(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getStandings(id));
    }

    /**
     * Returns all registered teams for a tournament.
     *
     * @param id the tournament primary key
     * @return list of team registration records
     */
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<RegistrationResponse>> getRegisteredTeams(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getRegisteredTeams(id));
    }
}
