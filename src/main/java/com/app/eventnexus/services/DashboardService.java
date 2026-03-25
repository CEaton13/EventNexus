package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.DashboardResponse;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for aggregating admin dashboard metrics.
 *
 * <p>Collects counts from multiple repositories in a single read transaction
 * so the dashboard endpoint makes no more DB round-trips than necessary.
 * Each metric is explained below:
 * <ul>
 *   <li>{@code tournamentsByStatus} — one entry per {@link TournamentStatus},
 *       ordered by the enum declaration order (DRAFT first, ARCHIVED last).</li>
 *   <li>{@code totalTeams} — total teams (all, regardless of tournaments).</li>
 *   <li>{@code activePlayers} — players where {@code is_active = true}.</li>
 *   <li>{@code upcomingMatchesNext7Days} — scheduled matches whose
 *       {@code scheduled_time} is in the next 7 calendar days and whose
 *       status is not {@code COMPLETED} or {@code BYE}.</li>
 * </ul>
 */
@Service
public class DashboardService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    public DashboardService(TournamentRepository tournamentRepository,
                            TeamRepository teamRepository,
                            PlayerRepository playerRepository,
                            MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Builds a {@link DashboardResponse} with aggregated counts across all entities.
     *
     * @return a populated dashboard summary DTO
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TournamentStatus status : TournamentStatus.values()) {
            byStatus.put(status.name(), tournamentRepository.countByStatus(status));
        }

        long totalTeams = teamRepository.count();
        long activePlayers = playerRepository.countByIsActive(true);

        LocalDateTime now = LocalDateTime.now();
        long upcoming = matchRepository.countUpcomingMatches(now, now.plusDays(7));

        return new DashboardResponse(byStatus, totalTeams, activePlayers, upcoming);
    }
}
