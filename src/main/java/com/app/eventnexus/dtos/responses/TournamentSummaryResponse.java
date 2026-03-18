package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.TournamentFormat;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.models.Tournament;

import java.time.LocalDateTime;

/**
 * Lightweight response DTO for tournament list views.
 * Omits heavy nested objects (full genre/venue detail) in favour of
 * summary fields suitable for cards and grids.
 */
public class TournamentSummaryResponse {

    private Long id;
    private String name;
    private String gameTitle;
    private TournamentStatus status;
    private TournamentFormat format;
    private Integer maxTeams;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long gameGenreId;
    private String gameGenreName;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code TournamentSummaryResponse} from a {@link Tournament} entity.
     *
     * @param tournament the source entity
     * @return a populated summary DTO
     */
    public static TournamentSummaryResponse from(Tournament tournament) {
        TournamentSummaryResponse dto = new TournamentSummaryResponse();
        dto.id = tournament.getId();
        dto.name = tournament.getName();
        dto.gameTitle = tournament.getGameTitle();
        dto.status = tournament.getStatus();
        dto.format = tournament.getFormat();
        dto.maxTeams = tournament.getMaxTeams();
        dto.startDate = tournament.getStartDate();
        dto.endDate = tournament.getEndDate();
        dto.gameGenreId = tournament.getGameGenre().getId();
        dto.gameGenreName = tournament.getGameGenre().getName();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TournamentSummaryResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public TournamentFormat getFormat() {
        return format;
    }

    public void setFormat(TournamentFormat format) {
        this.format = format;
    }

    public Integer getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getGameGenreId() {
        return gameGenreId;
    }

    public void setGameGenreId(Long gameGenreId) {
        this.gameGenreId = gameGenreId;
    }

    public String getGameGenreName() {
        return gameGenreName;
    }

    public void setGameGenreName(String gameGenreName) {
        this.gameGenreName = gameGenreName;
    }
}
