package com.app.eventnexus.services;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BracketService}.
 * All repositories are mocked — no database required.
 *
 * <p>Tests verify the single-elimination bracket algorithm for:
 * <ul>
 *   <li>Correct total match count (bracketSize - 1)</li>
 *   <li>Correct BYE match count (bracketSize - teamCount)</li>
 *   <li>Correct {@code next_match_id} links between rounds</li>
 *   <li>BYE winners pre-placed in the correct next-round slot</li>
 *   <li>Error cases (< 2 teams, tournament not found)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class BracketServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentTeamRepository tournamentTeamRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private BracketService bracketService;

    private Tournament tournament;

    /**
     * In-memory store tracking every distinct Match that has been saved.
     * Since {@code save()} returns the same object reference, mutations after
     * the first save are reflected here automatically.
     */
    private Map<Long, Match> savedMatches;
    private AtomicLong idSeq;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        tournament.setId(1L);

        savedMatches = new LinkedHashMap<>();
        idSeq = new AtomicLong(1);

        lenient().when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        // Assign a new ID on first save; subsequent saves of the same object just
        // refresh the map entry (it's the same reference, no state is lost).
        lenient().when(matchRepository.save(any(Match.class))).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(idSeq.getAndIncrement());
            }
            savedMatches.put(m.getId(), m);
            return m;
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates {@code count} TournamentTeam registrations with sequential seeds.
     */
    private List<TournamentTeam> makeRegistrations(int count) {
        List<TournamentTeam> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Team team = new Team();
            team.setId((long) (i + 1));
            TournamentTeam tt = new TournamentTeam(tournament, team);
            tt.setSeed(i + 1);
            list.add(tt);
        }
        return list;
    }

    private void stubTeams(int count) {
        when(tournamentTeamRepository.findApprovedTeamsOrderedBySeed(1L, RegistrationStatus.APPROVED))
                .thenReturn(makeRegistrations(count));
    }

    private long byeCount() {
        return savedMatches.values().stream()
                .filter(m -> m.getStatus() == MatchStatus.BYE)
                .count();
    }

    // ─── Match count and BYE count tests ──────────────────────────────────────

    @Test
    void generateBracket_2teams_creates1Match_0byes() {
        stubTeams(2);
        bracketService.generateBracket(1L);

        // bracketSize = 2, matches = 2 - 1 = 1
        assertThat(savedMatches).hasSize(1);
        assertThat(byeCount()).isZero();
    }

    @Test
    void generateBracket_3teams_creates3Matches_1bye() {
        stubTeams(3);
        bracketService.generateBracket(1L);

        // bracketSize = 4, matches = 4 - 1 = 3, byes = 4 - 3 = 1
        assertThat(savedMatches).hasSize(3);
        assertThat(byeCount()).isEqualTo(1);
    }

    @Test
    void generateBracket_5teams_creates7Matches_3byes() {
        stubTeams(5);
        bracketService.generateBracket(1L);

        // bracketSize = 8, matches = 8 - 1 = 7, byes = 8 - 5 = 3
        assertThat(savedMatches).hasSize(7);
        assertThat(byeCount()).isEqualTo(3);
    }

    @Test
    void generateBracket_8teams_creates7Matches_0byes() {
        stubTeams(8);
        bracketService.generateBracket(1L);

        // bracketSize = 8, matches = 8 - 1 = 7, byes = 0
        assertThat(savedMatches).hasSize(7);
        assertThat(byeCount()).isZero();
    }

    @Test
    void generateBracket_9teams_creates15Matches_7byes() {
        stubTeams(9);
        bracketService.generateBracket(1L);

        // bracketSize = 16, matches = 16 - 1 = 15, byes = 16 - 9 = 7
        assertThat(savedMatches).hasSize(15);
        assertThat(byeCount()).isEqualTo(7);
    }

    // ─── next_match_id link tests ──────────────────────────────────────────────

    @Test
    void generateBracket_nextMatchLinks_correct_for4Teams() {
        // 4 teams: bracketSize = 4, 2 rounds
        // Round 1: 2 matches (quarter-finals) → Round 2: 1 match (final)
        stubTeams(4);
        bracketService.generateBracket(1L);

        assertThat(savedMatches).hasSize(3);

        // The final (round 2) has no next match
        Match finalMatch = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Final match not found"));

        assertThat(finalMatch.getNextMatch()).isNull();

        // Both round-1 matches point to the final
        List<Match> round1 = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 1)
                .toList();

        assertThat(round1).hasSize(2);
        assertThat(round1).allMatch(m -> m.getNextMatch() != null);
        assertThat(round1).allMatch(m -> m.getNextMatch().getId().equals(finalMatch.getId()));
    }

    @Test
    void generateBracket_nextMatchLinks_correct_for8Teams() {
        // 8 teams: 3 rounds
        // Round 1 (4 matches) → Round 2 (2 matches) → Round 3 / Final (1 match)
        stubTeams(8);
        bracketService.generateBracket(1L);

        assertThat(savedMatches).hasSize(7);

        Match finalMatch = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 3)
                .findFirst()
                .orElseThrow();
        assertThat(finalMatch.getNextMatch()).isNull();

        List<Match> round2 = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 2)
                .toList();
        assertThat(round2).hasSize(2);
        assertThat(round2).allMatch(m -> m.getNextMatch() != null);
        assertThat(round2).allMatch(m -> m.getNextMatch().getId().equals(finalMatch.getId()));

        List<Match> round1 = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 1)
                .toList();
        assertThat(round1).hasSize(4);
        // Every round-1 match feeds into a round-2 match
        assertThat(round1).allMatch(m -> m.getNextMatch() != null
                && m.getNextMatch().getRoundNumber() == 2);
    }

    // ─── BYE winner pre-placement ──────────────────────────────────────────────

    @Test
    void generateBracket_byeWinner_prePlacedInNextRound() {
        // 3 teams: bracketSize = 4, 1 bye
        // Seed-1 team gets the BYE and should be pre-placed in the semi-final
        stubTeams(3);
        bracketService.generateBracket(1L);

        List<Match> round2 = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 2)
                .toList();
        assertThat(round2).hasSize(1);

        // Semi-final must have at least one team pre-filled (the BYE winner)
        Match semiFinal = round2.get(0);
        assertThat(semiFinal.getTeamA()).isNotNull();
    }

    @Test
    void generateBracket_byeMatch_hasTeamAAndWinnerSet() {
        stubTeams(3);
        bracketService.generateBracket(1L);

        List<Match> byeMatches = savedMatches.values().stream()
                .filter(m -> m.getStatus() == MatchStatus.BYE)
                .toList();

        assertThat(byeMatches).hasSize(1);
        Match byeMatch = byeMatches.get(0);
        assertThat(byeMatch.getTeamA()).isNotNull();
        assertThat(byeMatch.getWinner()).isNotNull();
        assertThat(byeMatch.getWinner()).isSameAs(byeMatch.getTeamA());
    }

    @Test
    void generateBracket_nonByeRound1Matches_haveBothTeamsFilled() {
        stubTeams(5);
        bracketService.generateBracket(1L);

        // With 5 teams: 3 BYEs + 1 regular match in round 1 (teams 4 vs 5)
        List<Match> round1Regular = savedMatches.values().stream()
                .filter(m -> m.getRoundNumber() == 1 && m.getStatus() != MatchStatus.BYE)
                .toList();

        assertThat(round1Regular).hasSize(1);
        assertThat(round1Regular.get(0).getTeamA()).isNotNull();
        assertThat(round1Regular.get(0).getTeamB()).isNotNull();
    }

    // ─── Error cases ──────────────────────────────────────────────────────────

    @Test
    void generateBracket_lessThan2Teams_throwsConflictException() {
        stubTeams(1);
        assertThatThrownBy(() -> bracketService.generateBracket(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("2 approved teams");
    }

    @Test
    void generateBracket_0Teams_throwsConflictException() {
        stubTeams(0);
        assertThatThrownBy(() -> bracketService.generateBracket(1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void generateBracket_tournamentNotFound_throwsResourceNotFoundException() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bracketService.generateBracket(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tournament");
    }
}