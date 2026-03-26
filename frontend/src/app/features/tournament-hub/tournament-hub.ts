import { Component, DestroyRef, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, switchMap, catchError, EMPTY } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  TournamentDetail,
  RegistrationResponse,
  StandingsResponse,
} from '../../shared/models/tournament.model';
import { BracketResponse } from '../../shared/models/match.model';
import { PublicTournamentService } from '../../core/services/public-tournament.service';
import { MatchService } from '../../core/services/match.service';
import { AuthDialogService } from '../../core/services/auth-dialog.service';
import { ThemeService } from '../../core/services/theme';
import { AuthService } from '../../core/services/auth';
import { MatchDetailDialog } from './match-detail-dialog/match-detail-dialog';

/**
 * TournamentHubComponent is the public-facing page for a single tournament.
 *
 * <p>It is accessible without authentication at {@code /t/:id} and shows
 * the bracket, standings, and registered teams. The genre theme is applied
 * on load and reset on destroy so navigating away restores the default theme.</p>
 *
 * <p>The "Register Your Team" CTA triggers the auth modal when the visitor
 * is not signed in.</p>
 */
@Component({
  selector: 'app-tournament-hub',
  standalone: false,
  templateUrl: './tournament-hub.html',
  styleUrl: './tournament-hub.scss',
})
export class TournamentHubComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly publicService = inject(PublicTournamentService);
  private readonly matchService = inject(MatchService);
  private readonly authDialogService = inject(AuthDialogService);
  private readonly themeService = inject(ThemeService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  readonly authService = inject(AuthService);

  readonly tournament = signal<TournamentDetail | null>(null);
  readonly bracket = signal<BracketResponse | null>(null);
  readonly standings = signal<StandingsResponse[]>([]);
  readonly registrations = signal<RegistrationResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  /** Which tab is active: 'all' | 'bracket' | 'standings' | 'teams' */
  readonly activeTab = signal<'all' | 'bracket' | 'standings' | 'teams'>('all');

  /** Flat list of all matches across all rounds from the loaded bracket. */
  readonly allMatches = computed(() => this.bracket()?.rounds.flatMap((r) => r.matches) ?? []);

  /** Matches currently in progress — shown in the Live Matches section. */
  readonly liveMatches = computed(() =>
    this.allMatches().filter((m) => m.status === 'IN_PROGRESS'),
  );

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set('Tournament not found.');
      this.loading.set(false);
      return;
    }
    this.loadAll(id);
    this.startStandingsPolling(id);
  }

  ngOnDestroy(): void {
    this.themeService.resetTheme();
  }

  /** Switches the active content tab. */
  setTab(tab: 'all' | 'bracket' | 'standings' | 'teams'): void {
    this.activeTab.set(tab);
  }

  /**
   * CTA for "Register Your Team".
   * Opens the auth modal when not authenticated, then navigates
   * the user to the org-scoped registration flow.
   */
  onRegister(): void {
    this.authDialogService
      .requireAuth()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((authenticated) => {
        if (!authenticated) return;
        const t = this.tournament();
        if (!t) return;
        // Redirect to the org-scoped tournament registration page.
        // TenantService will have been set after successful login.
        this.router.navigate(['/tournaments', t.id]);
      });
  }

  /**
   * Opens the MatchDetailDialog for the given match ID.
   * @param matchId Primary key of the match to display.
   */
  openMatchDetail(matchId: number): void {
    this.dialog.open(MatchDetailDialog, {
      data: { matchId },
      panelClass: 'dark-dialog',
      width: '420px',
    });
  }

  /** True when the tournament is open for registration. */
  get canRegister(): boolean {
    return this.tournament()?.status === 'REGISTRATION_OPEN';
  }

  private loadAll(id: number): void {
    this.loading.set(true);

    this.publicService.getById(id).subscribe({
      next: (t) => {
        this.tournament.set(t);
        if (t.gameGenre) {
          this.themeService.applyGenre(t.gameGenre);
        }
      },
      error: () => {
        this.error.set('Tournament not found or no longer available.');
        this.loading.set(false);
      },
    });

    this.publicService.getBracket(id).subscribe({
      next: (b) => this.bracket.set(b),
      error: () => this.bracket.set(null),
    });

    this.publicService.getStandings(id).subscribe({
      next: (s) => {
        this.standings.set(s);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.publicService.getRegisteredTeams(id).subscribe({
      next: (r) => this.registrations.set(r),
      error: () => this.registrations.set([]),
    });
  }

  /**
   * Starts a 30-second polling loop that refreshes standings for the given tournament.
   * The loop is automatically torn down when the component is destroyed via DestroyRef.
   * @param tournamentId The tournament whose standings to poll.
   */
  private startStandingsPolling(tournamentId: number): void {
    interval(30_000)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        switchMap(() =>
          this.publicService.getStandings(tournamentId).pipe(catchError(() => EMPTY)),
        ),
      )
      .subscribe({ next: (s) => this.standings.set(s) });
  }
}
