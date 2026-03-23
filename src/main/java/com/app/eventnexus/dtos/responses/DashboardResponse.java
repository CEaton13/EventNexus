package com.app.eventnexus.dtos.responses;

import java.util.Map;

/**
 * Response DTO for the admin dashboard summary endpoint.
 *
 * <p>Aggregates counts across several entities so the frontend
 * can display a high-level overview without making multiple API calls:
 * <ul>
 *   <li>{@code tournamentsByStatus} — one entry per tournament status,
 *       value is the count of tournaments in that state.</li>
 *   <li>{@code totalTeams} — total number of teams in the system.</li>
 *   <li>{@code activePlayers} — number of non-soft-deleted players.</li>
 *   <li>{@code upcomingMatchesNext7Days} — scheduled matches whose
 *       {@code scheduledTime} falls within the next 7 days and that
 *       are not yet completed or BYE.</li>
 * </ul>
 */
public class DashboardResponse {

    private Map<String, Long> tournamentsByStatus;
    private long totalTeams;
    private long activePlayers;
    private long upcomingMatchesNext7Days;

    public DashboardResponse() {}

    public DashboardResponse(Map<String, Long> tournamentsByStatus,
                             long totalTeams,
                             long activePlayers,
                             long upcomingMatchesNext7Days) {
        this.tournamentsByStatus = tournamentsByStatus;
        this.totalTeams = totalTeams;
        this.activePlayers = activePlayers;
        this.upcomingMatchesNext7Days = upcomingMatchesNext7Days;
    }

    public Map<String, Long> getTournamentsByStatus() { return tournamentsByStatus; }
    public long getTotalTeams() { return totalTeams; }
    public long getActivePlayers() { return activePlayers; }
    public long getUpcomingMatchesNext7Days() { return upcomingMatchesNext7Days; }
}
