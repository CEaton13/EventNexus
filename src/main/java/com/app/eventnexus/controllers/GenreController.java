package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.GameGenreRequest;
import com.app.eventnexus.dtos.responses.GameGenreResponse;
import com.app.eventnexus.services.GameGenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller exposing game genre data.
 * GET endpoints are public — no authentication required.
 * POST/PUT/DELETE require {@code TOURNAMENT_ADMIN}.
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

    /**
     * Creates a new game genre.
     * Only {@code TOURNAMENT_ADMIN} may create genres.
     *
     * @param request genre details
     * @return 201 Created with the new genre
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<GameGenreResponse> createGenre(@Valid @RequestBody GameGenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameGenreService.create(request));
    }

    /**
     * Updates an existing game genre's theme properties.
     * Only {@code TOURNAMENT_ADMIN} may update genres.
     *
     * @param id      the genre's primary key
     * @param request updated values
     * @return 200 OK with the updated genre, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<GameGenreResponse> updateGenre(@PathVariable Long id,
                                                         @Valid @RequestBody GameGenreRequest request) {
        return ResponseEntity.ok(gameGenreService.update(id, request));
    }

    /**
     * Deletes a game genre.
     * Only {@code TOURNAMENT_ADMIN} may delete genres.
     * Returns 409 Conflict if the genre is referenced by any existing tournament.
     *
     * @param id the genre's primary key
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        gameGenreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
