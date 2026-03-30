package com.app.eventnexus.repositories;

import com.app.eventnexus.models.TournamentFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TournamentFavorite}.
 */
public interface TournamentFavoriteRepository extends JpaRepository<TournamentFavorite, Long> {

    List<TournamentFavorite> findByUserId(Long userId);

    Optional<TournamentFavorite> findByUserIdAndTournamentId(Long userId, Long tournamentId);

    boolean existsByUserIdAndTournamentId(Long userId, Long tournamentId);

    void deleteByUserIdAndTournamentId(Long userId, Long tournamentId);
}
