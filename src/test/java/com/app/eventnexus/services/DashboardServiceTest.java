package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.DashboardResponse;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService}.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Long ORG_ID = 1L;

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private MatchRepository matchRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(ORG_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getDashboardSummary_returnsOrgScopedCounts() {
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.DRAFT)).thenReturn(2L);
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.REGISTRATION_OPEN)).thenReturn(1L);
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.REGISTRATION_CLOSED)).thenReturn(0L);
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.IN_PROGRESS)).thenReturn(3L);
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.COMPLETED)).thenReturn(5L);
        when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, TournamentStatus.ARCHIVED)).thenReturn(4L);

        when(teamRepository.countByOrganizationId(ORG_ID)).thenReturn(12L);
        when(playerRepository.countActiveByOrganizationId(ORG_ID)).thenReturn(48L);
        when(matchRepository.countUpcomingMatchesByOrganizationId(eq(ORG_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
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
    void getDashboardSummary_returnsZeros_whenOrgHasNoData() {
        for (TournamentStatus status : TournamentStatus.values()) {
            when(tournamentRepository.countByOrganizationIdAndStatus(ORG_ID, status)).thenReturn(0L);
        }
        when(teamRepository.countByOrganizationId(ORG_ID)).thenReturn(0L);
        when(playerRepository.countActiveByOrganizationId(ORG_ID)).thenReturn(0L);
        when(matchRepository.countUpcomingMatchesByOrganizationId(eq(ORG_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        DashboardResponse result = dashboardService.getDashboardSummary();

        assertThat(result.getTotalTeams()).isZero();
        assertThat(result.getActivePlayers()).isZero();
        assertThat(result.getUpcomingMatchesNext7Days()).isZero();
        result.getTournamentsByStatus().values()
                .forEach(count -> assertThat(count).isZero());
    }
}
