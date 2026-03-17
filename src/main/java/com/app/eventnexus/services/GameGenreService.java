package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.GameGenreResponse;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.repositories.GameGenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for reading game genre data.
 * Genres are seeded at schema creation time and represent the visual themes
 * available for tournaments (Fighting, Racing, FPS / Tactical, etc.).
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
}
