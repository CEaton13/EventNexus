package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.GameGenreResponse;
import com.app.eventnexus.services.GameGenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller exposing game genre data.
 * All endpoints are public — no authentication required.
 * Delegates entirely to {@link GameGenreService}.
 */
@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GameGenreService gameGenreService;

    public GenreController(GameGenreService gameGenreService) {
        this.gameGenreService = gameGenreService;
    }

    /**
     * Returns all available game genres.
     * Used by the frontend to populate genre selectors and apply tournament themes.
     *
     * @return 200 OK with a list of all genres
     */
    @GetMapping
    public ResponseEntity<List<GameGenreResponse>> getAllGenres() {
        return ResponseEntity.ok(gameGenreService.findAll());
    }

    /**
     * Returns a single game genre by its ID.
     *
     * @param id the genre's primary key
     * @return 200 OK with the genre, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameGenreResponse> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(gameGenreService.findById(id));
    }
}
