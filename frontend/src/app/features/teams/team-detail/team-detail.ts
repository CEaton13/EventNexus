import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { TeamResponse } from '../../../shared/models/team.model';
import { PlayerResponse } from '../../../shared/models/player.model';

/**
 * TeamDetailComponent shows team info, active player roster, and admin/manager controls.
 */
@Component({
  selector: 'app-team-detail',
  standalone: false,
  templateUrl: './team-detail.html',
  styleUrl: './team-detail.scss',
})
export class TeamDetail implements OnInit {
  readonly team = signal<TeamResponse | null>(null);
  readonly players = signal<PlayerResponse[]>([]);
  readonly loading = signal(false);

  private teamId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly teamService: TeamService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.teamId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.teamService.getById(this.teamId).subscribe({
      next: t => {
        this.team.set(t);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.teamService.getPlayers(this.teamId).subscribe(p => this.players.set(p));
  }

  /** True when the current user manages this team or is an admin. */
  canEdit(): boolean {
    const t = this.team();
    if (!t) return false;
    return this.authService.isAdmin() ||
      (this.authService.isTeamManager() && t.managerUsername === this.authService.user()?.username);
  }

  editTeam(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', this.teamId, 'edit']);
  }

  deleteTeam(): void {
    this.teamService.delete(this.teamId).subscribe({
      next: () => {
        this.snackBar.open('Team deleted', 'OK', { duration: 3000 });
        this.router.navigate([this.tenantService.currentOrgSlug(), 'teams']);
      },
    });
  }

  addPlayer(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', this.teamId, 'players', 'new']);
  }

  viewPlayer(playerId: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'players', playerId]);
  }

  backToList(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams']);
  }
}
