package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.PlayerRequest;
import com.app.eventnexus.dtos.responses.PlayerResponse;
import com.app.eventnexus.dtos.responses.PlayerStatsResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.PlayerService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller for player management.
 * Read endpoints are public. Write endpoints enforce authentication at the URL
 * level; ownership (team manager vs admin) is enforced inside {@link PlayerService}.
 * All business logic is delegated to {@link PlayerService}.
 */
@RestController
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Returns all players across all teams, including soft-deleted ones.
     *
     * @return 200 OK with a list of all players
     */
    @GetMapping("/api/players")
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        return ResponseEntity.ok(playerService.findAll());
    }

    /**
     * Returns a single player by ID. Soft-deleted players are still returned
     * with {@code "active": false}.
     *
     * @param id the player's primary key
     * @return 200 OK with the player, or 404 if not found
     */
    @GetMapping("/api/players/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.findById(id));
    }

    /**
     * Returns all per-tournament statistics for a player ordered by most recent tournament.
     *
     * @param id the player's primary key
     * @return 200 OK with a list of stats (may be empty if no tournaments yet)
     */
    @GetMapping("/api/players/{id}/stats")
    public ResponseEntity<List<PlayerStatsResponse>> getPlayerStats(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getStats(id));
    }

    /**
     * Adds a new player to a team.
     * The team is identified by {@code teamId} in the path.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN}.
     *
     * @param teamId         the team's primary key
     * @param request        player details
     * @param authentication the current user's security context
     * @return 201 Created with the new player
     */
    @PostMapping("/api/teams/{teamId}/players")
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER')")
    public ResponseEntity<PlayerResponse> createPlayer(@PathVariable Long teamId,
                                                       @RequestBody PlayerRequest request,
                                                       Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.create(teamId, request,
                        principal.getUserId(), principal.getRole()));
    }

    /**
     * Updates a player's profile.
     * The caller must be the manager of the player's team or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id             the player's primary key
     * @param request        updated values
     * @param authentication the current user's security context
     * @return 200 OK with the updated player, or 404 if not found
     */
    @PutMapping("/api/players/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerResponse> updatePlayer(@PathVariable Long id,
                                                       @RequestBody PlayerRequest request,
                                                       Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                playerService.update(id, request, principal.getUserId(), principal.getRole()));
    }

    /**
     * Soft-deletes a player (sets {@code active = false}).
     * The player record and all stats are preserved.
     * The caller must be the manager of the player's team or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id             the player's primary key
     * @param authentication the current user's security context
     * @return 204 No Content on success
     */
    @DeleteMapping("/api/players/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id,
                                             Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        playerService.delete(id, principal.getUserId(), principal.getRole());
        return ResponseEntity.noContent().build();
    }
}
