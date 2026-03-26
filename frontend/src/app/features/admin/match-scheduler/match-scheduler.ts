import { Component, OnInit, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { MatchService } from '../../../core/services/match.service';
import { TournamentService } from '../../../core/services/tournament.service';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';
import { TournamentDetail as TournamentDetailModel } from '../../../shared/models/tournament.model';
import { BracketResponse, BracketRound, MatchResponse } from '../../../shared/models/match.model';
import { VenueResponse } from '../../../shared/models/venue.model';

/** Per-match form state used in the schedule form map. */
export interface MatchFormState {
  venueId: number | null;
  scheduledTime: string;
}

/**
 * MatchSchedulerComponent provides admins a dedicated interface to schedule matches
 * for a tournament. It shows every match across all rounds and allows setting
 * the venue and time for each UNSCHEDULED match.
 * Requires TOURNAMENT_ADMIN role — enforced by the route guard.
 */
@Component({
  selector: 'app-match-scheduler',
  standalone: false,
  templateUrl: './match-scheduler.html',
  styleUrl: './match-scheduler.scss',
})
export class MatchScheduler implements OnInit {
  readonly tournament = signal<TournamentDetailModel | null>(null);
  readonly bracket = signal<BracketResponse | null>(null);
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);

  /**
   * Map of matchId → { venueId, scheduledTime } for the per-match scheduling forms.
   * Updated immutably using signal.update().
   */
  readonly scheduleForm = signal<Record<number, MatchFormState>>({});

  /** Flattened list of all matches across all rounds for convenience. */
  readonly allMatches = computed((): MatchResponse[] => {
    const b = this.bracket();
    if (!b) return [];
    return b.rounds.flatMap(r => r.matches);
  });

  /** Rounds with at least one schedulable or already-scheduled match. */
  readonly rounds = computed((): BracketRound[] => {
    return this.bracket()?.rounds ?? [];
  });

  private tournamentId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly matchService: MatchService,
    private readonly tournamentService: TournamentService,
    private readonly venueService: VenueService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  /**
   * Loads tournament detail, bracket, and venues in parallel.
   */
  load(): void {
    this.loading.set(true);
    forkJoin({
      tournament: this.tournamentService.getById(this.tournamentId),
      bracket: this.tournamentService.getBracket(this.tournamentId),
      venues: this.venueService.getAll(),
    }).subscribe({
      next: ({ tournament, bracket, venues }) => {
        this.tournament.set(tournament);
        this.bracket.set(bracket);
        this.venues.set(venues);
        this.initAllMatchForms(bracket);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load match data.', 'OK', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  /**
   * Initializes per-match form state for every UNSCHEDULED match in the bracket.
   * @param bracket The full bracket response.
   */
  private initAllMatchForms(bracket: BracketResponse): void {
    const initial: Record<number, MatchFormState> = {};
    for (const round of bracket.rounds) {
      for (const match of round.matches) {
        if (match.status === 'UNSCHEDULED') {
          initial[match.id] = { venueId: null, scheduledTime: '' };
        }
      }
    }
    this.scheduleForm.set(initial);
  }

  /**
   * Returns the current form state for a given match, initializing it if absent.
   * @param matchId Match primary key.
   */
  getMatchForm(matchId: number): MatchFormState {
    const current = this.scheduleForm();
    if (!current[matchId]) {
      this.initMatchForm(matchId);
      return { venueId: null, scheduledTime: '' };
    }
    return current[matchId];
  }

  /**
   * Initializes form state for a single match if not already present.
   * @param matchId Match primary key.
   */
  initMatchForm(matchId: number): void {
    this.scheduleForm.update(forms => {
      if (forms[matchId]) return forms;
      return { ...forms, [matchId]: { venueId: null, scheduledTime: '' } };
    });
  }

  /**
   * Updates the venueId for a specific match in the form state (immutable update).
   * @param matchId Match primary key.
   * @param venueId Selected venue primary key.
   */
  setMatchVenue(matchId: number, venueId: number | null): void {
    this.scheduleForm.update(forms => ({
      ...forms,
      [matchId]: { ...forms[matchId], venueId },
    }));
  }

  /**
   * Updates the scheduledTime for a specific match in the form state (immutable update).
   * @param matchId Match primary key.
   * @param scheduledTime ISO datetime string from datetime-local input.
   */
  setMatchTime(matchId: number, scheduledTime: string): void {
    this.scheduleForm.update(forms => ({
      ...forms,
      [matchId]: { ...forms[matchId], scheduledTime },
    }));
  }

  /**
   * Returns true if the given match's form has both a venue and a time selected.
   * @param matchId Match primary key.
   */
  canScheduleMatch(matchId: number): boolean {
    const form = this.scheduleForm()[matchId];
    return !!form && form.venueId !== null && !!form.scheduledTime;
  }

  /**
   * Schedules a match using the form values for that matchId.
   * On success, shows a snackbar and reloads the bracket.
   * @param matchId Match primary key.
   */
  scheduleMatch(matchId: number): void {
    const form = this.scheduleForm()[matchId];
    if (!form || !form.venueId || !form.scheduledTime) return;

    this.matchService.schedule(matchId, form.scheduledTime, form.venueId).subscribe({
      next: () => {
        this.snackBar.open('Match scheduled.', 'OK', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.snackBar.open('Failed to schedule match — check for conflicts.', 'OK', { duration: 4000 });
      },
    });
  }

  /**
   * Returns a human-readable team name or "TBD" when no team is assigned.
   * @param match The match to get team A's label for.
   */
  teamALabel(match: MatchResponse): string {
    return match.teamA ? match.teamA.name : 'TBD';
  }

  /**
   * Returns a human-readable team name or "TBD" when no team is assigned.
   * @param match The match to get team B's label for.
   */
  teamBLabel(match: MatchResponse): string {
    return match.teamB ? match.teamB.name : 'TBD';
  }

  /**
   * Navigates back to the tournament detail page.
   */
  backToTournament(): void {
    this.router.navigate([
      this.tenantService.currentOrgSlug(),
      'tournaments',
      this.tournamentId,
    ]);
  }
}
