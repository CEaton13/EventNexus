package com.app.eventnexus.services;

import com.app.eventnexus.dtos.responses.MatchResponse;
import com.app.eventnexus.enums.MatchStatus;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.InvalidStateTransitionException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Match;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling match scheduling and result recording.
 *
 * <h3>Conflict Detection</h3>
 * Before persisting a schedule, two checks run in sequence:
 * <ol>
 *   <li><b>Team conflict</b> — neither team may appear in another match whose
 *       window {@code [scheduled_time, scheduled_time + 2h]} overlaps the proposed window.</li>
 *   <li><b>Venue conflict</b> — the venue's concurrent active match count must not
 *       meet or exceed its {@code station_count} within the proposed window.</li>
 * </ol>
 *
 * <h3>Result Recording</h3>
 * Recording a result also advances the winner into the next bracket match,
 * filling whichever slot (A or B) corresponds to this match's position.
 */
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final VenueRepository venueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;

    public MatchService(MatchRepository matchRepository,
                        VenueRepository venueRepository,
                        TeamRepository teamRepository,
                        PlayerRepository playerRepository,
                        PlayerService playerService) {
        this.matchRepository = matchRepository;
        this.venueRepository = venueRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.playerService = playerService;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns the details of a single match by ID.
     *
     * @param matchId the match's primary key
     * @return the match as a response DTO
     * @throws ResourceNotFoundException if no match exists with the given ID
     */
    @Transactional(readOnly = true)
    public MatchResponse getById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));
        return MatchResponse.from(match);
    }

    // ─── Scheduling ────────────────────────────────────────────────────────────

    /**
     * Schedules a match by assigning a time and venue after conflict checks.
     * Both team slots must be filled before a match can be scheduled.
     *
     * <p>Conflict check 1 — team conflict: neither team may have another match
     * in the window {@code [scheduledTime, scheduledTime + 2h]}.
     *
     * <p>Conflict check 2 — venue conflict: the venue must have at least one
     * free station in the same window (concurrent active matches {@literal <} station count).
     *
     * @param matchId       the match's primary key
     * @param scheduledTime the proposed start time
     * @param venueId       the venue's primary key
     * @return the updated match as a response DTO
     * @throws ResourceNotFoundException      if the match or venue cannot be found
     * @throws ConflictException              if a team or venue conflict is detected
     * @throws InvalidStateTransitionException if the match is already COMPLETED or a BYE
     */
    @Transactional
    public MatchResponse scheduleMatch(Long matchId, LocalDateTime scheduledTime, Long venueId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.BYE) {
            throw new InvalidStateTransitionException(
                    "Cannot schedule a match with status " + match.getStatus());
        }

        if (match.getTeamA() == null || match.getTeamB() == null) {
            throw new ConflictException(
                    "Cannot schedule match " + matchId + ": both team slots must be filled first.");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));

        LocalDateTime endTime = scheduledTime.plusHours(2);
        Long teamAId = match.getTeamA().getId();
        Long teamBId = match.getTeamB().getId();

        // Check 1 — team conflict
        long teamConflicts = matchRepository.countTeamConflicts(matchId, teamAId, teamBId,
                scheduledTime, endTime);
        if (teamConflicts > 0) {
            throw new ConflictException(
                    "One or both teams already have a match scheduled in the window "
                            + scheduledTime + " – " + endTime + ".");
        }

        // Check 2 — venue conflict
        long venueConflicts = matchRepository.countVenueConflicts(matchId, venueId,
                scheduledTime, endTime);
        if (venueConflicts >= venue.getStationCount()) {
            throw new ConflictException(
                    "Venue '" + venue.getName() + "' is at full capacity ("
                            + venue.getStationCount() + " station(s)) during the requested window.");
        }

        match.setScheduledTime(scheduledTime);
        match.setVenue(venue);
        match.setStatus(MatchStatus.SCHEDULED);
        match.setUpdatedAt(LocalDateTime.now());

        return MatchResponse.from(matchRepository.save(match));
    }

    // ─── Result recording ──────────────────────────────────────────────────────

    /**
     * Records the result of a match, marking it {@code COMPLETED} and advancing
     * the winner into the next bracket match.
     *
     * <p>The winner is placed into the next match's team-A slot if this match has
     * an odd match number (1, 3, 5 …), or team-B slot if even (2, 4, 6 …).
     * This mirrors the slot assignment used during bracket generation.
     *
     * @param matchId  the match's primary key
     * @param winnerId the winning team's primary key; must be team A or team B of this match
     * @return the updated match as a response DTO
     * @throws ResourceNotFoundException       if the match or winner team cannot be found
     * @throws ConflictException               if the winner is not one of the match's teams
     * @throws InvalidStateTransitionException if the match is not in a recordable state
     */
    @Transactional
    public MatchResponse recordResult(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED
                && match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(
                    "Cannot record a result for a match with status " + match.getStatus()
                            + ". Match must be SCHEDULED or IN_PROGRESS.");
        }

        Team winner = teamRepository.findById(winnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", winnerId));

        // Validate the winner is actually one of the match's teams
        Long teamAId = match.getTeamA() != null ? match.getTeamA().getId() : null;
        Long teamBId = match.getTeamB() != null ? match.getTeamB().getId() : null;
        if (!winnerId.equals(teamAId) && !winnerId.equals(teamBId)) {
            throw new ConflictException(
                    "Team " + winnerId + " is not a participant in match " + matchId + ".");
        }

        match.setWinner(winner);
        match.setStatus(MatchStatus.COMPLETED);
        match.setUpdatedAt(LocalDateTime.now());
        matchRepository.save(match);

        // Record a win for every active player on the winning team,
        // and a loss for every active player on the losing team.
        Long tournamentId = match.getTournament().getId();
        Team loser = winnerId.equals(teamAId) ? match.getTeamB() : match.getTeamA();

        playerRepository.findByTeam_Id(winner.getId()).stream()
                .filter(p -> p.isActive())
                .forEach(p -> playerService.recordStats(p.getId(), tournamentId, true));

        if (loser != null) {
            playerRepository.findByTeam_Id(loser.getId()).stream()
                    .filter(p -> p.isActive())
                    .forEach(p -> playerService.recordStats(p.getId(), tournamentId, false));
        }

        // Advance the winner into the next bracket match
        Match nextMatch = match.getNextMatch();
        if (nextMatch != null) {
            // Odd match number (1-based) → fill slot A; even → fill slot B
            if (match.getMatchNumber() % 2 != 0) {
                nextMatch.setTeamA(winner);
            } else {
                nextMatch.setTeamB(winner);
            }
            nextMatch.setUpdatedAt(LocalDateTime.now());
            matchRepository.save(nextMatch);
        }

        return MatchResponse.from(match);
    }
}
