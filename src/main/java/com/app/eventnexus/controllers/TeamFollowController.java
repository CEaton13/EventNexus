package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.TeamFollowResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.TeamFollowService;
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
 * Thin REST controller for team follow management.
 * All endpoints require authentication; business logic is in {@link TeamFollowService}.
 */
@RestController
public class TeamFollowController {

    private final TeamFollowService teamFollowService;

    public TeamFollowController(TeamFollowService teamFollowService) {
        this.teamFollowService = teamFollowService;
    }

    /**
     * Follows a team on behalf of the authenticated user.
     *
     * @param teamId         the team's primary key
     * @param authentication the current authenticated caller
     * @return 201 Created with the follow record
     */
    @PostMapping("/api/teams/{teamId}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamFollowResponse> follow(@PathVariable Long teamId,
                                                     Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamFollowService.follow(userId, teamId));
    }

    /**
     * Unfollows a team on behalf of the authenticated user.
     *
     * @param teamId         the team's primary key
     * @param authentication the current authenticated caller
     * @return 204 No Content on success
     */
    @DeleteMapping("/api/teams/{teamId}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollow(@PathVariable Long teamId,
                                         Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        teamFollowService.unfollow(userId, teamId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the follow status of the authenticated user for the given team.
     *
     * @param teamId         the team's primary key
     * @param authentication the current authenticated caller
     * @return 200 OK with {@code { "following": true/false }}
     */
    @GetMapping("/api/teams/{teamId}/follow/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> followStatus(@PathVariable Long teamId,
                                                             Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(Map.of("following", teamFollowService.isFollowing(userId, teamId)));
    }

    /**
     * Returns all teams followed by the authenticated user.
     *
     * @param authentication the current authenticated caller
     * @return 200 OK with list of followed teams
     */
    @GetMapping("/api/users/me/follows")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamFollowResponse>> getMyFollows(Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(teamFollowService.getFollowedTeams(userId));
    }
}
