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
 * Unit tests for {@link TeamService}.
 * Repositories are mocked; no database required.
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

    private User manager;
    private User otherManager;
    private User admin;
    private Team team;

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
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsListOfTeamResponses() {
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(teamRepository.countActivePlayersByTeamId(1L)).thenReturn(3L);

        List<TeamResponse> result = teamService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alpha Squad");
        assertThat(result.get(0).getPlayerCount()).isEqualTo(3L);
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsTeamResponse_whenTeamExists() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.countActivePlayersByTeamId(1L)).thenReturn(0L);

        TeamResponse result = teamService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTag()).isEqualTo("ALPH");
    }

    @Test
    void findById_throwsResourceNotFoundException_whenTeamDoesNotExist() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team")
                .hasMessageContaining("99");
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsTeam_whenManagerExists() {
        TeamRequest request = new TeamRequest("Beta Force", "BETA", null, "EU");

        when(userRepository.findById(10L)).thenReturn(Optional.of(manager));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(teamRepository.countActivePlayersByTeamId(2L)).thenReturn(0L);

        TeamResponse result = teamService.create(request, 10L);

        assertThat(result.getName()).isEqualTo("Beta Force");
        assertThat(result.getManagerUsername()).isEqualTo("manager1");
    }

    @Test
    void create_throwsResourceNotFoundException_whenManagerDoesNotExist() {
        TeamRequest request = new TeamRequest("Beta Force", "BETA", null, "EU");
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.create(request, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_updatesAndReturnsTeam_whenCallerIsOwner() {
        TeamRequest request = new TeamRequest("Alpha Squad Pro", "ALPH", null, "NA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamRepository.countActivePlayersByTeamId(1L)).thenReturn(0L);

        TeamResponse result = teamService.update(1L, request, 10L, UserRole.TEAM_MANAGER);

        assertThat(result.getName()).isEqualTo("Alpha Squad Pro");
    }

    @Test
    void update_updatesAndReturnsTeam_whenCallerIsAdmin() {
        TeamRequest request = new TeamRequest("Admin Renamed", "ALPH", null, "NA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamRepository.countActivePlayersByTeamId(1L)).thenReturn(0L);

        TeamResponse result = teamService.update(1L, request, 99L, UserRole.TOURNAMENT_ADMIN);

        assertThat(result).isNotNull();
    }

    @Test
    void update_throwsUnauthorizedAccessException_whenCallerIsNotOwnerOrAdmin() {
        TeamRequest request = new TeamRequest("Hack Attempt", "HACK", null, "AS");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.update(1L, request, 20L, UserRole.TEAM_MANAGER))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Alpha Squad");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void update_throwsResourceNotFoundException_whenTeamDoesNotExist() {
        TeamRequest request = new TeamRequest("Ghost Team", "GHT", null, "NA");
        when(teamRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.update(5L, request, 10L, UserRole.TEAM_MANAGER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_deletesTeam_whenCallerIsOwner() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        teamService.delete(1L, 10L, UserRole.TEAM_MANAGER);

        verify(teamRepository).delete(team);
    }

    @Test
    void delete_deletesTeam_whenCallerIsAdmin() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        teamService.delete(1L, 99L, UserRole.TOURNAMENT_ADMIN);

        verify(teamRepository).delete(team);
    }

    @Test
    void delete_throwsUnauthorizedAccessException_whenCallerIsADifferentTeamManager() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.delete(1L, 20L, UserRole.TEAM_MANAGER))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Alpha Squad");

        verify(teamRepository, never()).delete(any());
    }

    @Test
    void delete_throwsResourceNotFoundException_whenTeamDoesNotExist() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.delete(99L, 10L, UserRole.TEAM_MANAGER))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
