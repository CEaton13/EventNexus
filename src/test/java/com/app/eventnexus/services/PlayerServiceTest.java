package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.PlayerRequest;
import com.app.eventnexus.dtos.responses.PlayerResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.UnauthorizedAccessException;
import com.app.eventnexus.models.Player;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.PlayerStatsRepository;
import com.app.eventnexus.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlayerService}.
 * Repositories are mocked; no database required.
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerStatsRepository playerStatsRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    private User manager;
    private User otherManager;
    private User admin;
    private Team team;
    private Player player;

    @BeforeEach
    void setUp() {
        manager = new User("manager1", "manager1@test.com", "hash", UserRole.TEAM_MANAGER);
        manager.setId(10L);

        otherManager = new User("manager2", "manager2@test.com", "hash", UserRole.TEAM_MANAGER);
        otherManager.setId(20L);

        admin = new User("admin1", "admin@test.com", "hash", UserRole.TOURNAMENT_ADMIN);
        admin.setId(99L);

        team = new Team("Alpha Squad", "ALPH", null, "NA", manager);
        team.setId(1L);

        player = new Player(team, "SniperX", "John Doe", "Sniper", "US", null);
        player.setId(5L);
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsListIncludingInactivePlayers() {
        Player inactive = new Player(team, "GhostPlayer", null, null, null, null);
        inactive.setId(6L);
        inactive.setActive(false);

        when(playerRepository.findAll()).thenReturn(List.of(player, inactive));

        List<PlayerResponse> result = playerService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.stream().anyMatch(p -> !p.isActive())).isTrue();
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsActivePlayer() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));

        PlayerResponse result = playerService.findById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getGamerTag()).isEqualTo("SniperX");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findById_returnsInactivePlayer_whenPlayerIsSoftDeleted() {
        player.setActive(false);
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));

        PlayerResponse result = playerService.findById(5L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void findById_throwsResourceNotFoundException_whenPlayerDoesNotExist() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player")
                .hasMessageContaining("99");
    }

    // ─── getStats ─────────────────────────────────────────────────────────────

    @Test
    void getStats_throwsResourceNotFoundException_whenPlayerDoesNotExist() {
        when(playerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> playerService.getStats(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Player");
    }

    @Test
    void getStats_returnsEmptyList_whenPlayerHasNoStats() {
        when(playerRepository.existsById(5L)).thenReturn(true);
        when(playerStatsRepository.findStatsWithTournamentByPlayerId(5L)).thenReturn(List.of());

        assertThat(playerService.getStats(5L)).isEmpty();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsPlayer_whenCallerIsTeamOwner() {
        PlayerRequest request = new PlayerRequest("FragMaster", "Jane Doe", "Support", "CA", null);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> {
            Player saved = inv.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        PlayerResponse result = playerService.create(1L, request, 10L, UserRole.TEAM_MANAGER);

        assertThat(result.getGamerTag()).isEqualTo("FragMaster");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void create_savesAndReturnsPlayer_whenCallerIsAdmin() {
        PlayerRequest request = new PlayerRequest("FragMaster", "Jane Doe", "Support", "CA", null);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> {
            Player saved = inv.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        PlayerResponse result = playerService.create(1L, request, 99L, UserRole.TOURNAMENT_ADMIN);

        assertThat(result).isNotNull();
    }

    @Test
    void create_throwsUnauthorizedAccessException_whenCallerDoesNotOwnTeam() {
        PlayerRequest request = new PlayerRequest("Hacker", null, null, null, null);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> playerService.create(1L, request, 20L, UserRole.TEAM_MANAGER))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Alpha Squad");

        verify(playerRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFoundException_whenTeamDoesNotExist() {
        PlayerRequest request = new PlayerRequest("X", null, null, null, null);
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.create(99L, request, 10L, UserRole.TEAM_MANAGER))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team");
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_updatesPlayer_whenCallerIsOwner() {
        PlayerRequest request = new PlayerRequest("SniperX Pro", "John Updated", "IGL", "US", null);

        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerResponse result = playerService.update(5L, request, 10L, UserRole.TEAM_MANAGER);

        assertThat(result.getGamerTag()).isEqualTo("SniperX Pro");
    }

    @Test
    void update_throwsUnauthorizedAccessException_whenCallerDoesNotOwnTeam() {
        PlayerRequest request = new PlayerRequest("SniperX Hacked", null, null, null, null);

        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> playerService.update(5L, request, 20L, UserRole.TEAM_MANAGER))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(playerRepository, never()).save(any());
    }

    // ─── delete (soft delete) ─────────────────────────────────────────────────

    @Test
    void delete_setsActiveFalse_andDoesNotHardDelete() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        playerService.delete(5L, 10L, UserRole.TEAM_MANAGER);

        assertThat(player.isActive()).isFalse();
        verify(playerRepository).save(player);       // saved with active=false
        verify(playerRepository, never()).delete(any()); // NOT hard deleted
    }

    @Test
    void delete_setsActiveFalse_whenCallerIsAdmin() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        playerService.delete(5L, 99L, UserRole.TOURNAMENT_ADMIN);

        assertThat(player.isActive()).isFalse();
    }

    @Test
    void delete_throwsUnauthorizedAccessException_whenCallerDoesNotOwnTeam() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> playerService.delete(5L, 20L, UserRole.TEAM_MANAGER))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Alpha Squad");

        assertThat(player.isActive()).isTrue();          // not soft-deleted
        verify(playerRepository, never()).save(any());   // nothing persisted
    }

    @Test
    void delete_throwsResourceNotFoundException_whenPlayerDoesNotExist() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.delete(99L, 10L, UserRole.TEAM_MANAGER))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
