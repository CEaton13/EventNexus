package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.StandingsResponse;
import com.app.eventnexus.services.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller exposing the tournament standings view.
 * This endpoint is public — no authentication required.
 */
@RestController
@RequestMapping("/api/tournaments")
public class StandingsController {

    private final TournamentService tournamentService;

    public StandingsController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /**
     * Returns the current standings for a tournament ordered by rank.
     * Data is sourced from the {@code tournament_standings} database view.
     * No authentication required.
     *
     * @param id the tournament's primary key
     * @return 200 OK with an ordered list of team standing rows, or 404 if not found
     */
    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingsResponse>> getStandings(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getStandings(id));
    }
}
