package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.TournamentTeam;

import java.time.LocalDateTime;

/**
 * Response DTO for a team manager's tournament registration.
 * Combines tournament summary, team identity, and registration status
 * into a single flat shape for the "My Registrations" page.
 */
public class MyRegistrationResponse {

    private Long tournamentId;
    private String tournamentName;
    private String gameTitle;
    private String tournamentStatus;
    private LocalDateTime startDate;
    private Long teamId;
    private String teamName;
    private String teamTag;
    private String registrationStatus;
    private LocalDateTime registeredAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code MyRegistrationResponse} from a {@link TournamentTeam} entity.
     * Both the tournament and team associations must be loaded before calling this.
     *
     * @param tt the tournament-team join record
     * @return a populated response DTO
     */
    public static MyRegistrationResponse from(TournamentTeam tt) {
        MyRegistrationResponse dto = new MyRegistrationResponse();
        dto.tournamentId = tt.getTournament().getId();
        dto.tournamentName = tt.getTournament().getName();
        dto.gameTitle = tt.getTournament().getGameTitle();
        dto.tournamentStatus = tt.getTournament().getStatus().name();
        dto.startDate = tt.getTournament().getStartDate();
        dto.teamId = tt.getTeam().getId();
        dto.teamName = tt.getTeam().getName();
        dto.teamTag = tt.getTeam().getTag();
        dto.registrationStatus = tt.getRegistrationStatus().name();
        dto.registeredAt = tt.getRegisteredAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public MyRegistrationResponse() {}

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getTournamentStatus() { return tournamentStatus; }
    public void setTournamentStatus(String tournamentStatus) { this.tournamentStatus = tournamentStatus; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
