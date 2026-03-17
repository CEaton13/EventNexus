package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.TeamRequest;
import com.app.eventnexus.dtos.responses.TeamResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.UnauthorizedAccessException;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing team data.
 *
 * <p>Ownership rules enforced here (not at the URL level):
 * <ul>
 *   <li>{@code update()} — caller must be the team's manager or a
 *       {@code TOURNAMENT_ADMIN}; otherwise throws
 *       {@link UnauthorizedAccessException}.</li>
 *   <li>{@code delete()} — same ownership rule as update.</li>
 * </ul>
 */
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all teams with their active player counts.
     *
     * @return list of all teams as response DTOs; never null
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll()
                .stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    /**
     * Returns a single team by its ID, including the active player count.
     *
     * @param id the team's primary key
     * @return the team as a response DTO
     * @throws ResourceNotFoundException if no team exists with the given ID
     */
    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
        return toResponseWithCount(team);
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new team. The authenticated user becomes the team manager.
     *
     * @param request   team details (name, tag, logoUrl, homeRegion)
     * @param managerId the ID of the authenticated user who is creating the team
     * @return the newly created team as a response DTO
     * @throws ResourceNotFoundException if the manager user cannot be found
     */
    @Transactional
    public TeamResponse create(TeamRequest request, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", managerId));

        Team team = new Team(
                request.getName(),
                request.getTag(),
                request.getLogoUrl(),
                request.getHomeRegion(),
                manager);

        return toResponseWithCount(teamRepository.save(team));
    }

    /**
     * Updates a team's mutable fields.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id            the team's primary key
     * @param request       updated values
     * @param requesterId   ID of the user making the request
     * @param requesterRole role of the user making the request
     * @return the updated team as a response DTO
     * @throws ResourceNotFoundException  if no team exists with the given ID
     * @throws UnauthorizedAccessException if the caller does not own the team and is not an admin
     */
    @Transactional
    public TeamResponse update(Long id, TeamRequest request, Long requesterId, UserRole requesterRole) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));

        verifyOwnership(team, requesterId, requesterRole);

        team.setName(request.getName());
        team.setTag(request.getTag());
        team.setLogoUrl(request.getLogoUrl());
        team.setHomeRegion(request.getHomeRegion());
        team.setUpdatedAt(LocalDateTime.now());

        return toResponseWithCount(teamRepository.save(team));
    }

    /**
     * Deletes a team by its ID.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id            the team's primary key
     * @param requesterId   ID of the user making the request
     * @param requesterRole role of the user making the request
     * @throws ResourceNotFoundException  if no team exists with the given ID
     * @throws UnauthorizedAccessException if the caller does not own the team and is not an admin
     */
    @Transactional
    public void delete(Long id, Long requesterId, UserRole requesterRole) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));

        verifyOwnership(team, requesterId, requesterRole);
        teamRepository.delete(team);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    /**
     * Throws {@link UnauthorizedAccessException} if the requester is neither
     * the team's manager nor a {@code TOURNAMENT_ADMIN}.
     */
    private void verifyOwnership(Team team, Long requesterId, UserRole requesterRole) {
        boolean isAdmin = requesterRole == UserRole.TOURNAMENT_ADMIN;
        boolean isOwner = team.getTeamManager() != null
                && team.getTeamManager().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to modify team '" + team.getName() + "'.");
        }
    }

    /**
     * Builds a {@link TeamResponse} from a team entity and populates the
     * active player count via a native query.
     */
    private TeamResponse toResponseWithCount(Team team) {
        TeamResponse dto = TeamResponse.from(team);
        dto.setPlayerCount(teamRepository.countActivePlayersByTeamId(team.getId()));
        return dto;
    }
}
