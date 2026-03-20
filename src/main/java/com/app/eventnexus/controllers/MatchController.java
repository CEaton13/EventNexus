package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.ResultRequest;
import com.app.eventnexus.dtos.requests.ScheduleMatchRequest;
import com.app.eventnexus.dtos.responses.MatchResponse;
import com.app.eventnexus.services.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller for match scheduling and result recording.
 * All endpoints require the {@code TOURNAMENT_ADMIN} role.
 * All business logic and conflict detection are delegated to {@link MatchService}.
 */
@RestController
@RequestMapping("/api/orgs/{orgSlug}/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Schedules a match by assigning a time and venue.
     * Runs team-conflict and venue-capacity checks before persisting.
     *
     * @param id      the match's primary key
     * @param request body containing {@code scheduledTime} and {@code venueId}
     * @return 200 OK with the updated match, or 409 if a conflict is detected
     */
    @PatchMapping("/{id}/schedule")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<MatchResponse> scheduleMatch(@PathVariable Long id,
                                                       @RequestBody ScheduleMatchRequest request) {
        return ResponseEntity.ok(
                matchService.scheduleMatch(id, request.getScheduledTime(), request.getVenueId()));
    }

    /**
     * Records the result of a match, marks it {@code COMPLETED}, and advances
     * the winner into the next bracket match.
     *
     * @param id      the match's primary key
     * @param request body containing {@code winnerId}
     * @return 200 OK with the updated match, or 409 if the winner is not a match participant
     */
    @PatchMapping("/{id}/result")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<MatchResponse> recordResult(@PathVariable Long id,
                                                      @RequestBody ResultRequest request) {
        return ResponseEntity.ok(matchService.recordResult(id, request.getWinnerId()));
    }
}
