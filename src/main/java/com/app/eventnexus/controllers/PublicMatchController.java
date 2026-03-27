package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.MatchResponse;
import com.app.eventnexus.services.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (no-auth) REST controller for reading individual match details.
 * Used by the spectator bracket viewer to load match info on click.
 */
@RestController
@RequestMapping("/api/matches")
public class PublicMatchController {

    private final MatchService matchService;

    public PublicMatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Returns the details of a single match by ID. No authentication required.
     *
     * @param id the match's primary key
     * @return 200 OK with the match DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getById(id));
    }
}
