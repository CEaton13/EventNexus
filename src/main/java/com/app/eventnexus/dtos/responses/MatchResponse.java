package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.MatchStatus;
import com.app.eventnexus.models.Match;
import com.app.eventnexus.models.Team;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single match in a tournament bracket.
 * Team slots ({@code teamA}, {@code teamB}) are null when the match
 * participant has not yet been determined.
 */
public class MatchResponse {

    private Long id;
    private Long tournamentId;
    private Integer roundNumber;
    private Integer matchNumber;
    private TeamSlot teamA;
    private TeamSlot teamB;
    private TeamSlot winner;
    private MatchStatus status;
    private LocalDateTime scheduledTime;
    private Long nextMatchId;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code MatchResponse} from a {@link Match} entity.
     *
     * @param match the source entity
     * @return a populated response DTO
     */
    public static MatchResponse from(Match match) {
        MatchResponse dto = new MatchResponse();
        dto.id = match.getId();
        dto.tournamentId = match.getTournament().getId();
        dto.roundNumber = match.getRoundNumber();
        dto.matchNumber = match.getMatchNumber();
        dto.teamA = match.getTeamA() != null ? TeamSlot.from(match.getTeamA()) : null;
        dto.teamB = match.getTeamB() != null ? TeamSlot.from(match.getTeamB()) : null;
        dto.winner = match.getWinner() != null ? TeamSlot.from(match.getWinner()) : null;
        dto.status = match.getStatus();
        dto.scheduledTime = match.getScheduledTime();
        dto.nextMatchId = match.getNextMatch() != null ? match.getNextMatch().getId() : null;
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public MatchResponse() {
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

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    public TeamSlot getTeamA() {
        return teamA;
    }

    public void setTeamA(TeamSlot teamA) {
        this.teamA = teamA;
    }

    public TeamSlot getTeamB() {
        return teamB;
    }

    public void setTeamB(TeamSlot teamB) {
        this.teamB = teamB;
    }

    public TeamSlot getWinner() {
        return winner;
    }

    public void setWinner(TeamSlot winner) {
        this.winner = winner;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Long getNextMatchId() {
        return nextMatchId;
    }

    public void setNextMatchId(Long nextMatchId) {
        this.nextMatchId = nextMatchId;
    }

    // ─── Nested DTO ────────────────────────────────────────────────────────────

    /**
     * Compact team representation used within a match slot.
     */
    public static class TeamSlot {

        private Long id;
        private String name;
        private String tag;

        public static TeamSlot from(Team team) {
            TeamSlot slot = new TeamSlot();
            slot.id = team.getId();
            slot.name = team.getName();
            slot.tag = team.getTag();
            return slot;
        }

        public TeamSlot() {
        }

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

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
