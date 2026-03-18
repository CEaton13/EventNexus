package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.models.TournamentTeam;

import java.time.LocalDateTime;

/**
 * Response DTO representing a team's registration entry for a tournament.
 */
public class RegistrationResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long teamId;
    private String teamName;
    private Integer seed;
    private RegistrationStatus registrationStatus;
    private LocalDateTime registeredAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code RegistrationResponse} from a {@link TournamentTeam} entity.
     *
     * @param entry the source entity
     * @return a populated response DTO
     */
    public static RegistrationResponse from(TournamentTeam entry) {
        RegistrationResponse dto = new RegistrationResponse();
        dto.id = entry.getId();
        dto.tournamentId = entry.getTournament().getId();
        dto.tournamentName = entry.getTournament().getName();
        dto.teamId = entry.getTeam().getId();
        dto.teamName = entry.getTeam().getName();
        dto.seed = entry.getSeed();
        dto.registrationStatus = entry.getRegistrationStatus();
        dto.registeredAt = entry.getRegisteredAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public RegistrationResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}