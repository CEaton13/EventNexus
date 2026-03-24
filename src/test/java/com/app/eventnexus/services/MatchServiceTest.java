package com.app.eventnexus.services;

import com.app.eventnexus.enums.MatchStatus;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.InvalidStateTransitionException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Match;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MatchService}.
 * Focus areas: team conflict detection, venue conflict detection,
 * result recording validation, and state-transition guards.
 */
@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PlayerService playerService;

    @InjectMocks
    private MatchService matchService;

    private Match match;
    private Team teamA;
    private Team teamB;
    private Venue venue;
    private Tournament tournament;
    private final LocalDateTime scheduledTime = LocalDateTime.of(2025, 6, 1, 10, 0);

    @BeforeEach
    void setUp() {
        teamA = new Team(); teamA.setId(1L); teamA.setName("Alpha");
        teamB = new Team(); teamB.setId(2L); teamB.setName("Beta");

        tournament = new Tournament(); tournament.setId(10L);

        match = new Match();
        match.setId(100L);
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        match.setStatus(MatchStatus.UNSCHEDULED);
        match.setMatchNumber(1);
        match.setRoundNumber(1);
        match.setTournament(tournament);

        venue = new Venue();
        venue.setId(5L);
        venue.setName("Main Hall");
        venue.setStationCount(4);
    }

    // ─── scheduleMatch ─────────────────────────────────────────────────────────

    @Test
    void scheduleMatch_succeeds_whenNoConflicts() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(matchRepository.countTeamConflicts(eq(100L), eq(1L), eq(2L), any(), any())).thenReturn(0L);
        when(matchRepository.countVenueConflicts(eq(100L), eq(5L), any(), any())).thenReturn(0L);
        when(matchRepository.save(any())).thenReturn(match);

        matchService.scheduleMatch(100L, scheduledTime, 5L);

        verify(matchRepository).save(match);
    }

    @Test
    void scheduleMatch_throwsConflict_whenTeamAlreadyBooked() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(matchRepository.countTeamConflicts(eq(100L), eq(1L), eq(2L), any(), any())).thenReturn(1L);

        assertThatThrownBy(() -> matchService.scheduleMatch(100L, scheduledTime, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("teams");
    }

    @Test
    void scheduleMatch_throwsConflict_whenVenueAtCapacity() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(matchRepository.countTeamConflicts(eq(100L), eq(1L), eq(2L), any(), any())).thenReturn(0L);
        // venue has 4 stations but 4 active — at capacity
        when(matchRepository.countVenueConflicts(eq(100L), eq(5L), any(), any())).thenReturn(4L);

        assertThatThrownBy(() -> matchService.scheduleMatch(100L, scheduledTime, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void scheduleMatch_throwsInvalidStateTransition_forCompletedMatch() {
        match.setStatus(MatchStatus.COMPLETED);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.scheduleMatch(100L, scheduledTime, 5L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void scheduleMatch_throwsConflict_whenTeamSlotsNotFilled() {
        match.setTeamB(null);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.scheduleMatch(100L, scheduledTime, 5L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("slots");
    }

    @Test
    void scheduleMatch_throwsNotFound_whenMatchMissing() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.scheduleMatch(999L, scheduledTime, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── recordResult ──────────────────────────────────────────────────────────

    @Test
    void recordResult_succeeds_andAdvancesWinnerToNextMatch() {
        match.setStatus(MatchStatus.SCHEDULED);
        Match nextMatch = new Match();
        nextMatch.setId(200L);
        match.setNextMatch(nextMatch);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(teamA));
        when(playerRepository.findByTeam_Id(1L)).thenReturn(List.of());
        when(playerRepository.findByTeam_Id(2L)).thenReturn(List.of());
        when(matchRepository.save(any())).thenReturn(match);

        matchService.recordResult(100L, 1L);

        verify(matchRepository).save(match);
        verify(matchRepository).save(nextMatch);
    }

    @Test
    void recordResult_throwsConflict_whenWinnerNotInMatch() {
        match.setStatus(MatchStatus.SCHEDULED);
        Team outsider = new Team(); outsider.setId(99L);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(teamRepository.findById(99L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> matchService.recordResult(100L, 99L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not a participant");
    }

    @Test
    void recordResult_throwsInvalidStateTransition_whenMatchNotScheduledOrInProgress() {
        match.setStatus(MatchStatus.UNSCHEDULED);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.recordResult(100L, 1L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
