package com.app.eventnexus.dtos.responses;

/**
 * Response DTO representing one team's row in the {@code tournament_standings} view.
 */
public class StandingsResponse {

    private Long teamId;
    private String teamName;
    private String teamTag;
    private String logoUrl;
    private long wins;
    private long losses;
    private long points;
    private long rank;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public StandingsResponse() {
    }

    public StandingsResponse(Long teamId, String teamName, String teamTag, String logoUrl,
                             long wins, long losses, long points, long rank) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamTag = teamTag;
        this.logoUrl = logoUrl;
        this.wins = wins;
        this.losses = losses;
        this.points = points;
        this.rank = rank;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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

    public String getTeamTag() {
        return teamTag;
    }

    public void setTeamTag(String teamTag) {
        this.teamTag = teamTag;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public long getWins() {
        return wins;
    }

    public void setWins(long wins) {
        this.wins = wins;
    }

    public long getLosses() {
        return losses;
    }

    public void setLosses(long losses) {
        this.losses = losses;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }
}
