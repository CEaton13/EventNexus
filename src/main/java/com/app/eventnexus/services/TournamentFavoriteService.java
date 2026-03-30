package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.TournamentFavoriteResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.TournamentFavorite;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.TournamentFavoriteRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing tournament favorite (bookmark) relationships.
 * Users may favorite any tournament; favorites are not org-scoped.
 */
@Service
public class TournamentFavoriteService {

    private final TournamentFavoriteRepository tournamentFavoriteRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    public TournamentFavoriteService(TournamentFavoriteRepository tournamentFavoriteRepository,
                                     TournamentRepository tournamentRepository,
                                     UserRepository userRepository) {
        this.tournamentFavoriteRepository = tournamentFavoriteRepository;
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Favorites a tournament on behalf of the given user.
     *
     * @param userId       the authenticated user's ID
     * @param tournamentId the tournament to favorite
     * @return the created favorite as a response DTO
     * @throws ResourceNotFoundException if the tournament does not exist
     * @throws ConflictException         if the user already favorited this tournament
     */
    @Transactional
    public TournamentFavoriteResponse favorite(Long userId, Long tournamentId) {
        if (tournamentFavoriteRepository.existsByUserIdAndTournamentId(userId, tournamentId)) {
            throw new ConflictException("You have already favorited this tournament.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));
        return TournamentFavoriteResponse.from(
                tournamentFavoriteRepository.save(new TournamentFavorite(user, tournament)));
    }

    /**
     * Removes a tournament favorite on behalf of the given user.
     *
     * @param userId       the authenticated user's ID
     * @param tournamentId the tournament to unfavorite
     * @throws ResourceNotFoundException if no favorite relationship exists
     */
    @Transactional
    public void unfavorite(Long userId, Long tournamentId) {
        if (!tournamentFavoriteRepository.existsByUserIdAndTournamentId(userId, tournamentId)) {
            throw new ResourceNotFoundException(
                    "Favorite relationship not found for tournament " + tournamentId);
        }
        tournamentFavoriteRepository.deleteByUserIdAndTournamentId(userId, tournamentId);
    }

    /**
     * Returns all tournaments favorited by the given user.
     *
     * @param userId the authenticated user's ID
     * @return list of tournament-favorite response DTOs
     */
    @Transactional(readOnly = true)
    public List<TournamentFavoriteResponse> getFavoriteTournaments(Long userId) {
        return tournamentFavoriteRepository.findByUserId(userId)
                .stream()
                .map(TournamentFavoriteResponse::from)
                .toList();
    }

    /**
     * Returns whether the given user has favorited the given tournament.
     *
     * @param userId       the authenticated user's ID
     * @param tournamentId the tournament to check
     * @return {@code true} if the user has favorited the tournament
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long tournamentId) {
        return tournamentFavoriteRepository.existsByUserIdAndTournamentId(userId, tournamentId);
    }
}
