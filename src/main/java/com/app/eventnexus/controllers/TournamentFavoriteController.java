package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.TournamentFavoriteResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.TournamentFavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Thin REST controller for tournament favorite management.
 * All endpoints require authentication; business logic is in {@link TournamentFavoriteService}.
 */
@RestController
public class TournamentFavoriteController {

    private final TournamentFavoriteService tournamentFavoriteService;

    public TournamentFavoriteController(TournamentFavoriteService tournamentFavoriteService) {
        this.tournamentFavoriteService = tournamentFavoriteService;
    }

    /**
     * Favorites a tournament on behalf of the authenticated user.
     *
     * @param tournamentId   the tournament's primary key
     * @param authentication the current authenticated caller
     * @return 201 Created with the favorite record
     */
    @PostMapping("/api/tournaments/{tournamentId}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentFavoriteResponse> favorite(@PathVariable Long tournamentId,
                                                               Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentFavoriteService.favorite(userId, tournamentId));
    }

    /**
     * Removes a tournament favorite on behalf of the authenticated user.
     *
     * @param tournamentId   the tournament's primary key
     * @param authentication the current authenticated caller
     * @return 204 No Content on success
     */
    @DeleteMapping("/api/tournaments/{tournamentId}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfavorite(@PathVariable Long tournamentId,
                                           Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        tournamentFavoriteService.unfavorite(userId, tournamentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the favorite status of the authenticated user for the given tournament.
     *
     * @param tournamentId   the tournament's primary key
     * @param authentication the current authenticated caller
     * @return 200 OK with {@code { "favorited": true/false }}
     */
    @GetMapping("/api/tournaments/{tournamentId}/favorite/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> favoriteStatus(@PathVariable Long tournamentId,
                                                               Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(Map.of("favorited",
                tournamentFavoriteService.isFavorited(userId, tournamentId)));
    }

    /**
     * Returns all tournaments favorited by the authenticated user.
     *
     * @param authentication the current authenticated caller
     * @return 200 OK with list of favorited tournaments
     */
    @GetMapping("/api/users/me/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TournamentFavoriteResponse>> getMyFavorites(
            Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(tournamentFavoriteService.getFavoriteTournaments(userId));
    }
}
