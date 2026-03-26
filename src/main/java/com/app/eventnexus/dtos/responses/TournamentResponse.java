package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.TournamentFormat;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.models.Tournament;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;

/**
 * Full response DTO for a tournament, including genre and venue details.
 */
public class TournamentResponse {

    private Long id;
    private String name;
    private String description;
    private String gameTitle;
    private TournamentStatus status;
    private TournamentFormat format;
    private Integer maxTeams;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GameGenreResponse gameGenre;
    private VenueResponse venue;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code TournamentResponse} from a {@link Tournament} entity.
     *
     * @param tournament the source entity
     * @return a populated response DTO
     */
    public static TournamentResponse from(Tournament tournament) {
        TournamentResponse dto = new TournamentResponse();
        dto.id = tournament.getId();
        dto.name = tournament.getName();
        dto.description = tournament.getDescription();
        dto.gameTitle = tournament.getGameTitle();
        dto.status = tournament.getStatus();
        dto.format = tournament.getFormat();
        dto.maxTeams = tournament.getMaxTeams();
        dto.registrationStart = tournament.getRegistrationStart();
        dto.registrationEnd = tournament.getRegistrationEnd();
        dto.startDate = tournament.getStartDate();
        dto.endDate = tournament.getEndDate();
        dto.gameGenre = GameGenreResponse.from(tournament.getGameGenre());
        try {
            dto.venue = tournament.getVenue() != null ? VenueResponse.from(tournament.getVenue()) : null;
        } catch (EntityNotFoundException e) {
            // Venue FK exists but the referenced row was deleted — treat as no venue
            dto.venue = null;
        }
        dto.createdByUsername = tournament.getCreatedBy() != null ? tournament.getCreatedBy().getUsername() : null;
        dto.createdAt = tournament.getCreatedAt();
        dto.updatedAt = tournament.getUpdatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TournamentResponse() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(LocalDateTime registrationStart) {
        this.registrationStart = registrationStart;
    }

    public LocalDateTime getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(LocalDateTime registrationEnd) {
        this.registrationEnd = registrationEnd;
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

    public GameGenreResponse getGameGenre() {
        return gameGenre;
    }

    public void setGameGenre(GameGenreResponse gameGenre) {
        this.gameGenre = gameGenre;
    }

    public VenueResponse getVenue() {
        return venue;
    }

    public void setVenue(VenueResponse venue) {
        this.venue = venue;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
