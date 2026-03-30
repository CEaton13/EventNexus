package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.TeamFollowResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.TeamFollow;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.TeamFollowRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing team follow relationships.
 * Users may follow any team; follows are not org-scoped.
 */
@Service
public class TeamFollowService {

    private final TeamFollowRepository teamFollowRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamFollowService(TeamFollowRepository teamFollowRepository,
                             TeamRepository teamRepository,
                             UserRepository userRepository) {
        this.teamFollowRepository = teamFollowRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    /**
     * Follows a team on behalf of the given user.
     *
     * @param userId the authenticated user's ID
     * @param teamId the team to follow
     * @return the created follow as a response DTO
     * @throws ResourceNotFoundException if the team does not exist
     * @throws ConflictException         if the user already follows this team
     */
    @Transactional
    public TeamFollowResponse follow(Long userId, Long teamId) {
        if (teamFollowRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new ConflictException("You are already following this team.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        return TeamFollowResponse.from(teamFollowRepository.save(new TeamFollow(user, team)));
    }

    /**
     * Unfollows a team on behalf of the given user.
     *
     * @param userId the authenticated user's ID
     * @param teamId the team to unfollow
     * @throws ResourceNotFoundException if no follow relationship exists
     */
    @Transactional
    public void unfollow(Long userId, Long teamId) {
        if (!teamFollowRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new ResourceNotFoundException("Follow relationship not found for team " + teamId);
        }
        teamFollowRepository.deleteByUserIdAndTeamId(userId, teamId);
    }

    /**
     * Returns all teams followed by the given user.
     *
     * @param userId the authenticated user's ID
     * @return list of team-follow response DTOs
     */
    @Transactional(readOnly = true)
    public List<TeamFollowResponse> getFollowedTeams(Long userId) {
        return teamFollowRepository.findByUserId(userId)
                .stream()
                .map(TeamFollowResponse::from)
                .toList();
    }

    /**
     * Returns whether the given user is currently following the given team.
     *
     * @param userId the authenticated user's ID
     * @param teamId the team to check
     * @return {@code true} if the user follows the team
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long teamId) {
        return teamFollowRepository.existsByUserIdAndTeamId(userId, teamId);
    }
}
