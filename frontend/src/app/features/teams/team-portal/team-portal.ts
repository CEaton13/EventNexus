import { Component, OnInit, signal, computed, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { TeamResponse } from '../../../shared/models/team.model';
import { PlayerResponse } from '../../../shared/models/player.model';
import { RegistrationResponse } from '../../../shared/models/tournament.model';

/**
 * TeamPortal is a dashboard for team managers showing team overview,
 * roster, and tournament registration status.
 *
 * NOTE: Tournament registrations for a specific team require a
 * GET /teams/{id}/tournaments endpoint which does not yet exist in the API spec.
 * The registrations section is scaffolded but left empty until that endpoint
 * is available.
 */
@Component({
  selector: 'app-team-portal',
  standalone: false,
  templateUrl: './team-portal.html',
  styleUrl: './team-portal.scss',
})
export class TeamPortal implements OnInit {
  /** The loaded team, or null while loading. */
  readonly team = signal<TeamResponse | null>(null);

  /** Active players on this team. */
  readonly players = signal<PlayerResponse[]>([]);

  /**
   * Tournament registrations for this team.
   * TODO: requires GET /teams/{id}/tournaments endpoint — currently no backend
   * support for fetching registrations scoped to a team. Left empty until
   * that endpoint is added to the API spec.
   */
  readonly registrations = signal<RegistrationResponse[]>([]);

  /** True while any data is loading. */
  readonly loading = signal(false);

  /** Registrations where the team has been approved. */
  readonly approvedTournaments = computed(() =>
    this.registrations().filter(r => r.registrationStatus === 'APPROVED')
  );

  /** Registrations still awaiting admin review. */
  readonly pendingRegistrations = computed(() =>
    this.registrations().filter(r => r.registrationStatus === 'PENDING')
  );

  /** Registrations the admin has rejected. */
  readonly rejectedRegistrations = computed(() =>
    this.registrations().filter(r => r.registrationStatus === 'REJECTED')
  );

  private teamId!: number;
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly teamService: TeamService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.teamId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  /** Loads team info and roster. */
  load(): void {
    this.loading.set(true);

    this.teamService.getById(this.teamId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: t => {
          this.team.set(t);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Failed to load team data.', 'Dismiss', { duration: 4000 });
          this.loading.set(false);
        },
      });

    this.teamService.getPlayers(this.teamId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: players => this.players.set(players),
        error: () => this.snackBar.open('Failed to load roster.', 'Dismiss', { duration: 4000 }),
      });
  }

  /** True when the current user manages this team or is an admin. */
  canEdit(): boolean {
    const t = this.team();
    if (!t) return false;
    return this.authService.isAdmin() ||
      (this.authService.isTeamManager() && t.managerUsername === this.authService.user()?.username);
  }

  /** Navigate to the team edit form. */
  editTeam(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', this.teamId, 'edit']);
  }

  /** Navigate to a player's detail page. */
  viewPlayer(playerId: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'players', playerId]);
  }

  /** Navigate back to the team detail page. */
  back(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', this.teamId]);
  }
}
