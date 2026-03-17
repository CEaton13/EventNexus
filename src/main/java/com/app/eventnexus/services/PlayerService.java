package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.PlayerRequest;
import com.app.eventnexus.dtos.responses.PlayerResponse;
import com.app.eventnexus.dtos.responses.PlayerStatsResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.UnauthorizedAccessException;
import com.app.eventnexus.models.Player;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.PlayerStatsRepository;
import com.app.eventnexus.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing player data.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Players are never hard-deleted — {@code delete()} sets
 *       {@code isActive = false} (soft delete).</li>
 *   <li>Ownership check: the caller must be the manager of the player's team
 *       or a {@code TOURNAMENT_ADMIN}; otherwise
 *       {@link UnauthorizedAccessException} is thrown.</li>
 * </ul>
 */
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final TeamRepository teamRepository;

    public PlayerService(PlayerRepository playerRepository,
                         PlayerStatsRepository playerStatsRepository,
                         TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.playerStatsRepository = playerStatsRepository;
        this.teamRepository = teamRepository;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all players (active and inactive) across all teams.
     *
     * @return list of all players as response DTOs; never null
     */
    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll()
                .stream()
                .map(PlayerResponse::from)
                .toList();
    }

    /**
     * Returns a single player by ID regardless of active status.
     * Soft-deleted players are still returned with {@code active = false}.
     *
     * @param id the player's primary key
     * @return the player as a response DTO
     * @throws ResourceNotFoundException if no player exists with the given ID
     */
    @Transactional(readOnly = true)
    public PlayerResponse findById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
        return PlayerResponse.from(player);
    }

    /**
     * Returns all per-tournament statistics for a player, ordered by tournament
     * start date descending.
     *
     * @param playerId the player's primary key
     * @return list of stats DTOs enriched with tournament name; empty if no stats recorded yet
     * @throws ResourceNotFoundException if no player exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<PlayerStatsResponse> getStats(Long playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("Player", playerId);
        }
        return playerStatsRepository.findStatsWithTournamentByPlayerId(playerId)
                .stream()
                .map(PlayerStatsResponse::from)
                .toList();
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Adds a new player to a team.
     * The caller must be the team's manager or a {@code TOURNAMENT_ADMIN}.
     *
     * @param teamId        the team to add the player to
     * @param request       player details
     * @param requesterId   ID of the authenticated user
     * @param requesterRole role of the authenticated user
     * @return the newly created player as a response DTO
     * @throws ResourceNotFoundException  if the team does not exist
     * @throws UnauthorizedAccessException if the caller is not the team's manager or admin
     */
    @Transactional
    public PlayerResponse create(Long teamId, PlayerRequest request,
                                 Long requesterId, UserRole requesterRole) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        verifyTeamOwnership(team, requesterId, requesterRole);

        Player player = new Player(
                team,
                request.getGamerTag(),
                request.getRealName(),
                request.getPosition(),
                request.getCountry(),
                request.getAvatarUrl());

        return PlayerResponse.from(playerRepository.save(player));
    }

    /**
     * Updates a player's profile fields.
     * The caller must be the manager of the player's team or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id            the player's primary key
     * @param request       updated values
     * @param requesterId   ID of the authenticated user
     * @param requesterRole role of the authenticated user
     * @return the updated player as a response DTO
     * @throws ResourceNotFoundException  if no player exists with the given ID
     * @throws UnauthorizedAccessException if the caller is not authorized
     */
    @Transactional
    public PlayerResponse update(Long id, PlayerRequest request,
                                 Long requesterId, UserRole requesterRole) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));

        verifyTeamOwnership(player.getTeam(), requesterId, requesterRole);

        player.setGamerTag(request.getGamerTag());
        player.setRealName(request.getRealName());
        player.setPosition(request.getPosition());
        player.setCountry(request.getCountry());
        player.setAvatarUrl(request.getAvatarUrl());
        player.setUpdatedAt(LocalDateTime.now());

        return PlayerResponse.from(playerRepository.save(player));
    }

    /**
     * Soft-deletes a player by setting {@code isActive = false}.
     * The player record and all associated stats are preserved in the database.
     * The caller must be the manager of the player's team or a {@code TOURNAMENT_ADMIN}.
     *
     * @param id            the player's primary key
     * @param requesterId   ID of the authenticated user
     * @param requesterRole role of the authenticated user
     * @throws ResourceNotFoundException  if no player exists with the given ID
     * @throws UnauthorizedAccessException if the caller is not authorized
     */
    @Transactional
    public void delete(Long id, Long requesterId, UserRole requesterRole) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));

        verifyTeamOwnership(player.getTeam(), requesterId, requesterRole);

        player.setActive(false);
        player.setUpdatedAt(LocalDateTime.now());
        playerRepository.save(player);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    /**
     * Throws {@link UnauthorizedAccessException} if the requester is neither
     * the team's manager nor a {@code TOURNAMENT_ADMIN}.
     */
    private void verifyTeamOwnership(Team team, Long requesterId, UserRole requesterRole) {
        boolean isAdmin = requesterRole == UserRole.TOURNAMENT_ADMIN;
        boolean isOwner = team.getTeamManager() != null
                && team.getTeamManager().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to manage players for team '" + team.getName() + "'.");
        }
    }
}
