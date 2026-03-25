package com.app.eventnexus.dtos.responses;

import java.util.List;

/**
 * Response DTO representing the full bracket for a tournament.
 * Matches are grouped into rounds; each round is ordered by match number.
 */
public class BracketResponse {

    private Long tournamentId;
    private String tournamentName;
    private int totalRounds;
    private int bracketSize;
    private List<BracketRound> rounds;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public BracketResponse() {
    }

    public BracketResponse(Long tournamentId, String tournamentName,
                           int totalRounds, int bracketSize, List<BracketRound> rounds) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.totalRounds = totalRounds;
        this.bracketSize = bracketSize;
        this.rounds = rounds;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getBracketSize() {
        return bracketSize;
    }

    public void setBracketSize(int bracketSize) {
        this.bracketSize = bracketSize;
    }

    public List<BracketRound> getRounds() {
        return rounds;
    }

    public void setRounds(List<BracketRound> rounds) {
        this.rounds = rounds;
    }

    // ─── Nested DTO ────────────────────────────────────────────────────────────

    /**
     * A single round in the bracket, containing an ordered list of matches.
     */
    public static class BracketRound {

        private int roundNumber;
        private String roundLabel;
        private List<MatchResponse> matches;

        public BracketRound() {
        }

        public BracketRound(int roundNumber, String roundLabel, List<MatchResponse> matches) {
            this.roundNumber = roundNumber;
            this.roundLabel = roundLabel;
            this.matches = matches;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
        }

        public String getRoundLabel() {
            return roundLabel;
        }

        public void setRoundLabel(String roundLabel) {
            this.roundLabel = roundLabel;
        }

        public List<MatchResponse> getMatches() {
            return matches;
        }

        public void setMatches(List<MatchResponse> matches) {
            this.matches = matches;
        }
    }
}
