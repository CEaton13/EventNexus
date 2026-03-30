package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.PlayerResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller for linking/unlinking a user account to a player profile.
 *
 * <p>Endpoints are restricted to team managers (for their own team's players) and
 * tournament admins. Ownership validation is delegated to {@link PlayerService}.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerLinkController {

    private final PlayerService playerService;

    public PlayerLinkController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Links a user account to a player profile.
     * The {@code userId} is taken from the path so an admin or team manager can
     * link any valid user, not just themselves.
     *
     * @param playerId       the player's primary key
     * @param userId         the user to link to the player
     * @param authentication the current authenticated caller
     * @return 200 OK with the updated player
     */
    @PatchMapping("/{playerId}/link/{userId}")
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER')")
    public ResponseEntity<PlayerResponse> linkUser(@PathVariable Long playerId,
                                                   @PathVariable Long userId,
                                                   Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                playerService.linkUser(playerId, userId,
                        principal.getUserId(), principal.getRole()));
    }

    /**
     * Removes the user-account link from a player profile.
     *
     * @param playerId       the player's primary key
     * @param authentication the current authenticated caller
     * @return 200 OK with the updated player (userId will be null)
     */
    @DeleteMapping("/{playerId}/link")
    @PreAuthorize("hasAnyRole('TOURNAMENT_ADMIN', 'TEAM_MANAGER')")
    public ResponseEntity<PlayerResponse> unlinkUser(@PathVariable Long playerId,
                                                     Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                playerService.unlinkUser(playerId,
                        principal.getUserId(), principal.getRole()));
    }
}
