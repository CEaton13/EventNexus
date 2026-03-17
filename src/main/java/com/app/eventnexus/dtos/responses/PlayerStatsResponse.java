package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.repositories.PlayerStatsRepository.StatsWithTournament;

/**
 * Response DTO for a player's statistics within a single tournament.
 */
public class PlayerStatsResponse {

    private Long tournamentId;
    private String tournamentName;
    private int wins;
    private int losses;
    private int mvpCount;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code PlayerStatsResponse} from a repository projection.
     *
     * @param projection the native query result containing stats and tournament name
     * @return a populated response DTO
     */
    public static PlayerStatsResponse from(StatsWithTournament projection) {
        PlayerStatsResponse dto = new PlayerStatsResponse();
        dto.tournamentId = projection.getTournamentId();
        dto.tournamentName = projection.getTournamentName();
        dto.wins = projection.getWins() != null ? projection.getWins() : 0;
        dto.losses = projection.getLosses() != null ? projection.getLosses() : 0;
        dto.mvpCount = projection.getMvpCount() != null ? projection.getMvpCount() : 0;
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public PlayerStatsResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getMvpCount() { return mvpCount; }
    public void setMvpCount(int mvpCount) { this.mvpCount = mvpCount; }
}
