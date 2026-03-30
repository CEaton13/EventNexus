package com.app.eventnexus.repositories;

import com.app.eventnexus.models.TeamFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TeamFollow}.
 */
public interface TeamFollowRepository extends JpaRepository<TeamFollow, Long> {

    List<TeamFollow> findByUserId(Long userId);

    Optional<TeamFollow> findByUserIdAndTeamId(Long userId, Long teamId);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);

    void deleteByUserIdAndTeamId(Long userId, Long teamId);
}
