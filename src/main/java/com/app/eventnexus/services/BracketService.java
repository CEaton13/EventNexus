package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.BracketResponse;
import com.app.eventnexus.dtos.responses.MatchResponse;
import com.app.eventnexus.enums.MatchStatus;
import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Match;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.TournamentTeam;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating and retrieving single-elimination brackets.
 *
 * <h3>Bracket Generation Algorithm</h3>
 * <ol>
 *   <li>Load all APPROVED teams for the tournament, ordered by seed (nulls last).</li>
 *   <li>Calculate bracket size: smallest power of 2 ≥ team count.</li>
 *   <li>Calculate byes: {@code bracketSize - teamCount}.</li>
 *   <li>Create match shells from the final round back to round 1, linking each match
 *       to the round-above match via {@code next_match_id}.</li>
 *   <li>Fill round 1: top-seeded teams receive BYE matches; the rest are paired
 *       sequentially. BYE winners are pre-placed into their round-2 slot.</li>
 * </ol>
 */
@Service
public class BracketService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final MatchRepository matchRepository;

    public BracketService(TournamentRepository tournamentRepository,
                          TournamentTeamRepository tournamentTeamRepository,
                          MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.matchRepository = matchRepository;
    }

    // ─── Bracket Generation ────────────────────────────────────────────────────

    /**
     * Generates a single-elimination bracket for the given tournament.
     * Should be called exactly once when the tournament transitions to {@code IN_PROGRESS}.
     *
     * @param tournamentId the tournament's primary key
     * @throws ResourceNotFoundException if the tournament does not exist
     * @throws ConflictException         if there are fewer than 2 approved teams
     */
    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));

        List<Team> teams = tournamentTeamRepository
                .findApprovedTeamsOrderedBySeed(tournamentId, RegistrationStatus.APPROVED)
                .stream()
                .map(TournamentTeam::getTeam)
                .toList();

        int numTeams = teams.size();
        if (numTeams < 2) {
            throw new ConflictException(
                    "Cannot generate bracket: at least 2 approved teams are required, found " + numTeams);
        }

        int bracketSize = nextPowerOfTwo(numTeams);
        int numRounds = Integer.numberOfTrailingZeros(bracketSize); // log2(bracketSize)
        int byesCount = bracketSize - numTeams;

        LocalDateTime now = LocalDateTime.now();

        // ── Step 1: Create match shells from last round → round 1 ──────────────
        // Keys are 1-based round numbers; values are ordered lists of saved Match entities.
        Map<Integer, List<Match>> matchesByRound = new HashMap<>();

        for (int round = numRounds; round >= 1; round--) {
            int numMatchesInRound = bracketSize >> round; // bracketSize / 2^round
            List<Match> nextRoundMatches = matchesByRound.get(round + 1);
            List<Match> roundMatches = new ArrayList<>(numMatchesInRound);

            for (int i = 0; i < numMatchesInRound; i++) {
                // Each pair of matches in round r feeds into one match in round r+1
                Match nextMatch = (nextRoundMatches != null) ? nextRoundMatches.get(i / 2) : null;

                Match match = new Match();
                match.setTournament(tournament);
                match.setRoundNumber(round);
                match.setMatchNumber(i + 1); // 1-based
                match.setStatus(MatchStatus.UNSCHEDULED);
                match.setNextMatch(nextMatch);
                match.setCreatedAt(now);
                match.setUpdatedAt(now);

                roundMatches.add(matchRepository.save(match));
            }

            matchesByRound.put(round, roundMatches);
        }

        // ── Step 2: Fill round 1 with teams ────────────────────────────────────
        List<Match> round1Matches = matchesByRound.get(1);

        // First byesCount slots: BYE matches for top-seeded teams
        for (int i = 0; i < byesCount; i++) {
            Match byeMatch = round1Matches.get(i);
            Team byeTeam = teams.get(i);

            byeMatch.setTeamA(byeTeam);
            byeMatch.setWinner(byeTeam);
            byeMatch.setStatus(MatchStatus.BYE);
            byeMatch.setUpdatedAt(now);

            // Pre-place the BYE winner into the correct slot of their next-round match
            Match nextMatch = byeMatch.getNextMatch();
            if (nextMatch != null) {
                if (i % 2 == 0) {
                    nextMatch.setTeamA(byeTeam);
                } else {
                    nextMatch.setTeamB(byeTeam);
                }
                matchRepository.save(nextMatch);
            }

            matchRepository.save(byeMatch);
        }

        // Remaining slots: pair the non-BYE teams sequentially
        List<Team> remainingTeams = teams.subList(byesCount, numTeams);
        int matchSlot = byesCount;
        for (int i = 0; i < remainingTeams.size(); i += 2) {
            Match match = round1Matches.get(matchSlot++);
            match.setTeamA(remainingTeams.get(i));
            match.setTeamB(remainingTeams.get(i + 1));
            match.setUpdatedAt(now);
            matchRepository.save(match);
        }
    }

    // ─── Bracket Read ──────────────────────────────────────────────────────────

    /**
     * Returns the full bracket for a tournament, with matches grouped into labelled rounds.
     *
     * @param tournamentId the tournament's primary key
     * @return the bracket response with all rounds and matches
     * @throws ResourceNotFoundException if the tournament does not exist
     */
    @Transactional(readOnly = true)
    public BracketResponse getBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));

        List<Match> allMatches = matchRepository
                .findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(tournamentId);

        // Determine bracket size from the match data (total matches = bracketSize - 1)
        int totalMatches = allMatches.size();
        int bracketSize = totalMatches > 0 ? nextPowerOfTwo(totalMatches + 1) : 0;
        int numRounds = bracketSize > 0 ? Integer.numberOfTrailingZeros(bracketSize) : 0;

        // Group matches by round number
        Map<Integer, List<MatchResponse>> byRound = allMatches.stream()
                .collect(Collectors.groupingBy(
                        Match::getRoundNumber,
                        Collectors.mapping(MatchResponse::from, Collectors.toList())));

        List<BracketResponse.BracketRound> rounds = new ArrayList<>();
        for (int round = 1; round <= numRounds; round++) {
            List<MatchResponse> matchesInRound = byRound.getOrDefault(round, List.of());
            String label = roundLabel(round, numRounds);
            rounds.add(new BracketResponse.BracketRound(round, label, matchesInRound));
        }

        return new BracketResponse(
                tournament.getId(),
                tournament.getName(),
                numRounds,
                bracketSize,
                rounds);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns the smallest power of 2 that is >= {@code n}.
     *
     * @param n a positive integer
     * @return the next power of two
     */
    private int nextPowerOfTwo(int n) {
        if (n <= 1) return 1;
        return Integer.highestOneBit(n - 1) << 1;
    }

    /**
     * Returns a human-readable label for a round number given the total rounds.
     * E.g., "Final", "Semi-Final", "Quarter-Final", "Round of 16", or generic "Round N".
     *
     * @param roundNumber the 1-based round number
     * @param totalRounds total rounds in the bracket
     * @return a display label
     */
    private String roundLabel(int roundNumber, int totalRounds) {
        int roundsFromEnd = totalRounds - roundNumber;
        return switch (roundsFromEnd) {
            case 0 -> "Final";
            case 1 -> "Semi-Final";
            case 2 -> "Quarter-Final";
            default -> "Round of " + (int) Math.pow(2, roundsFromEnd + 1);
        };
    }
}
