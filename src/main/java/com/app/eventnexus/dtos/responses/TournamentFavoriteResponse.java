package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.TournamentFavorite;

import java.time.LocalDateTime;

/**
 * Response DTO for a tournament-favorite relationship.
 * Includes enough tournament info for the "My Tournaments" watchlist.
 */
public class TournamentFavoriteResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private String gameTitle;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code TournamentFavoriteResponse} from a {@link TournamentFavorite} entity.
     *
     * @param favorite the source entity (tournament must be loaded)
     * @return a populated response DTO
     */
    public static TournamentFavoriteResponse from(TournamentFavorite favorite) {
        TournamentFavoriteResponse dto = new TournamentFavoriteResponse();
        dto.id = favorite.getId();
        dto.tournamentId = favorite.getTournament().getId();
        dto.tournamentName = favorite.getTournament().getName();
        dto.gameTitle = favorite.getTournament().getGameTitle();
        dto.status = favorite.getTournament().getStatus().name();
        dto.startDate = favorite.getTournament().getStartDate();
        dto.createdAt = favorite.getCreatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TournamentFavoriteResponse() {}

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
