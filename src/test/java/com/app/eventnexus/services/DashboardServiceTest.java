package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.DashboardResponse;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService}.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private MatchRepository matchRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardSummary_returnsAggregatedCounts() {
        // Stub tournament counts per status
        when(tournamentRepository.countByStatus(TournamentStatus.DRAFT)).thenReturn(2L);
        when(tournamentRepository.countByStatus(TournamentStatus.REGISTRATION_OPEN)).thenReturn(1L);
        when(tournamentRepository.countByStatus(TournamentStatus.REGISTRATION_CLOSED)).thenReturn(0L);
        when(tournamentRepository.countByStatus(TournamentStatus.IN_PROGRESS)).thenReturn(3L);
        when(tournamentRepository.countByStatus(TournamentStatus.COMPLETED)).thenReturn(5L);
        when(tournamentRepository.countByStatus(TournamentStatus.ARCHIVED)).thenReturn(4L);

        when(teamRepository.count()).thenReturn(12L);
        when(playerRepository.countByIsActive(true)).thenReturn(48L);
        when(matchRepository.countUpcomingMatches(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(7L);

        DashboardResponse result = dashboardService.getDashboardSummary();

        assertThat(result.getTotalTeams()).isEqualTo(12L);
        assertThat(result.getActivePlayers()).isEqualTo(48L);
        assertThat(result.getUpcomingMatchesNext7Days()).isEqualTo(7L);

        assertThat(result.getTournamentsByStatus()).containsEntry("DRAFT", 2L);
        assertThat(result.getTournamentsByStatus()).containsEntry("IN_PROGRESS", 3L);
        assertThat(result.getTournamentsByStatus()).containsEntry("COMPLETED", 5L);
        assertThat(result.getTournamentsByStatus()).hasSize(TournamentStatus.values().length);
    }

    @Test
    void getDashboardSummary_returnsZeros_whenNothingExists() {
        for (TournamentStatus status : TournamentStatus.values()) {
            when(tournamentRepository.countByStatus(status)).thenReturn(0L);
        }
        when(teamRepository.count()).thenReturn(0L);
        when(playerRepository.countByIsActive(true)).thenReturn(0L);
        when(matchRepository.countUpcomingMatches(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        DashboardResponse result = dashboardService.getDashboardSummary();

        assertThat(result.getTotalTeams()).isZero();
        assertThat(result.getActivePlayers()).isZero();
        assertThat(result.getUpcomingMatchesNext7Days()).isZero();
        result.getTournamentsByStatus().values()
                .forEach(count -> assertThat(count).isZero());
    }
}
