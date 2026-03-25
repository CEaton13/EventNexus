package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.GameGenreRequest;
import com.app.eventnexus.dtos.responses.GameGenreResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.GameGenre;
import com.app.eventnexus.repositories.GameGenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing game genre data.
 * Genres represent the visual themes available for tournaments
 * (Fighting, Racing, FPS / Tactical, etc.).
 * Read operations are open to all users; write operations require TOURNAMENT_ADMIN.
 */
@Service
public class GameGenreService {

    private final GameGenreRepository gameGenreRepository;

    public GameGenreService(GameGenreRepository gameGenreRepository) {
        this.gameGenreRepository = gameGenreRepository;
    }

    /**
     * Returns all available game genres ordered by database insertion order.
     *
     * @return list of all genres as response DTOs; never null, may be empty
     */
    @Transactional(readOnly = true)
    public List<GameGenreResponse> findAll() {
        return gameGenreRepository.findAll()
                .stream()
                .map(GameGenreResponse::from)
                .toList();
    }

    /**
     * Returns a single game genre by its ID.
     *
     * @param id the genre's primary key
     * @return the matching genre as a response DTO
     * @throws ResourceNotFoundException if no genre exists with the given ID
     */
    @Transactional(readOnly = true)
    public GameGenreResponse findById(Long id) {
        return gameGenreRepository.findById(id)
                .map(GameGenreResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("GameGenre", id));
    }

    /**
     * Creates a new game genre.
     *
     * @param request genre details (name, colors, font, backgroundStyle, iconPackKey)
     * @return the newly created genre as a response DTO
     * @throws ConflictException if a genre with the same name already exists
     */
    @Transactional
    public GameGenreResponse create(GameGenreRequest request) {
        if (gameGenreRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("A genre named '" + request.getName() + "' already exists.");
        }
        GameGenre genre = new GameGenre();
        applyRequest(genre, request);
        genre.setCreatedAt(LocalDateTime.now());
        genre.setUpdatedAt(LocalDateTime.now());
        return GameGenreResponse.from(gameGenreRepository.save(genre));
    }

    /**
     * Updates an existing game genre's theme properties.
     *
     * @param id      the genre's primary key
     * @param request updated values
     * @return the updated genre as a response DTO
     * @throws ResourceNotFoundException if no genre exists with the given ID
     * @throws ConflictException         if the new name conflicts with another genre
     */
    @Transactional
    public GameGenreResponse update(Long id, GameGenreRequest request) {
        GameGenre genre = gameGenreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GameGenre", id));
        if (!genre.getName().equalsIgnoreCase(request.getName())
                && gameGenreRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("A genre named '" + request.getName() + "' already exists.");
        }
        applyRequest(genre, request);
        genre.setUpdatedAt(LocalDateTime.now());
        return GameGenreResponse.from(gameGenreRepository.save(genre));
    }

    /**
     * Deletes a game genre by its ID.
     *
     * @param id the genre's primary key
     * @throws ResourceNotFoundException if no genre exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        if (!gameGenreRepository.existsById(id)) {
            throw new ResourceNotFoundException("GameGenre", id);
        }
        gameGenreRepository.deleteById(id);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void applyRequest(GameGenre genre, GameGenreRequest request) {
        genre.setName(request.getName());
        genre.setPrimaryColor(request.getPrimaryColor());
        genre.setSecondaryColor(request.getSecondaryColor());
        genre.setAccentColor(request.getAccentColor());
        genre.setBackgroundStyle(request.getBackgroundStyle());
        genre.setFontFamily(request.getFontFamily());
        genre.setIconPackKey(request.getIconPackKey());
        genre.setHeroImageUrl(request.getHeroImageUrl());
    }
}
