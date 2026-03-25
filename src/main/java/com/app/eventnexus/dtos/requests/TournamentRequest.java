package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.TournamentFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a tournament.
 */
public class TournamentRequest {

    @NotBlank(message = "Tournament name is required")
    @Size(max = 200, message = "Tournament name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 200, message = "Game title must not exceed 200 characters")
    private String gameTitle;

    @NotNull(message = "Tournament format is required")
    private TournamentFormat format;

    @NotNull(message = "Max teams is required")
    @Min(value = 2, message = "Tournament must allow at least 2 teams")
    @Max(value = 512, message = "Tournament cannot exceed 512 teams")
    private Integer maxTeams;

    @NotNull(message = "Registration start date is required")
    private LocalDateTime registrationStart;

    @NotNull(message = "Registration end date is required")
    private LocalDateTime registrationEnd;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Venue is required")
    private Long venueId;

    @NotNull(message = "Game genre is required")
    private Long gameGenreId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public Long getGameGenreId() {
        return gameGenreId;
    }

    public void setGameGenreId(Long gameGenreId) {
        this.gameGenreId = gameGenreId;
    }
}
