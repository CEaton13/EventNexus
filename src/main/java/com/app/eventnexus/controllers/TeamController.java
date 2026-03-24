package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.TeamRequest;
import com.app.eventnexus.dtos.responses.PageResponse;
import com.app.eventnexus.dtos.responses.TeamResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller for team management.
 * Read endpoints are public; write endpoints require authentication.
 * Ownership enforcement (manager vs admin) is handled inside {@link TeamService}.
 * All business logic is delegated to {@link TeamService}.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Returns a page of teams with active player counts.
     * Supports {@code ?page=0&size=20&sort=name} query parameters.
     *
     * @param pageable pagination and sort parameters (default: 20 per page, sorted by name)
     * @return 200 OK with a page of teams
     */
    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> getAllTeams(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(teamService.findAll(pageable)));
    }

    /**
     * Returns a single team by its ID, including active player count.
     *
     * @param id the team's primary key
     * @return 200 OK with the team, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    /**
     * Creates a new team. The authenticated user automatically becomes the team manager.
     * Both {@code TEAM_MANAGER} and {@code TOURNAMENT_ADMIN} roles may create teams.
     *
     * @param request        team details (name, tag, logoUrl, homeRegion)
     * @param authentication the current user's security context
     * @return 201 Created with the new team
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER')")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request,
                                                   Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.create(request, principal.getUserId()));
    }

    /**
     * Updates an existing team's details.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN};
     * the service throws 403 otherwise.
     *
     * @param id             the team's primary key
     * @param request        updated values
     * @param authentication the current user's security context
     * @return 200 OK with the updated team, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long id,
                                                   @Valid @RequestBody TeamRequest request,
                                                   Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                teamService.update(id, request, principal.getUserId(), principal.getRole()));
    }

    /**
     * Deletes a team by its ID.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN};
     * the service throws 403 otherwise.
     *
     * @param id             the team's primary key
     * @param authentication the current user's security context
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id,
                                           Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        teamService.delete(id, principal.getUserId(), principal.getRole());
        return ResponseEntity.noContent().build();
    }
}
