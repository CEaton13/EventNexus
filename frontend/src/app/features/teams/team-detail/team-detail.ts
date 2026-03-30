import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamService } from '../../../core/services/team.service';
import { TeamFollowService } from '../../../core/services/team-follow.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { TeamResponse } from '../../../shared/models/team.model';
import { PlayerResponse } from '../../../shared/models/player.model';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';

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
  readonly following = signal(false);
  readonly followLoading = signal(false);

  private teamId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly teamService: TeamService,
    private readonly teamFollowService: TeamFollowService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.teamId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
    if (this.authService.isAuthenticated()) {
      this.loadFollowStatus();
    }
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

  private loadFollowStatus(): void {
    this.teamFollowService.isFollowing(this.teamId).subscribe({
      next: ({ following }) => this.following.set(following),
    });
  }

  toggleFollow(): void {
    if (this.followLoading()) return;
    this.followLoading.set(true);
    const action = this.following()
      ? this.teamFollowService.unfollow(this.teamId)
      : this.teamFollowService.follow(this.teamId);

    action.subscribe({
      next: () => {
        this.following.set(!this.following());
        this.followLoading.set(false);
        this.snackBar.open(
          this.following() ? 'Following team' : 'Unfollowed team',
          'OK',
          { duration: 2000 },
        );
      },
      error: () => this.followLoading.set(false),
    });
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
    const teamName = this.team()?.name ?? 'this team';
    this.dialog.open(ConfirmDialog, {
      panelClass: 'dark-dialog',
      data: {
        title: 'Delete Team',
        message: `Permanently delete "${teamName}"? All players and tournament registrations will be removed. This cannot be undone.`,
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
      },
    }).afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.teamService.delete(this.teamId).subscribe({
        next: () => {
          this.snackBar.open('Team deleted', 'OK', { duration: 3000 });
          this.router.navigate([this.tenantService.currentOrgSlug(), 'teams']);
        },
      });
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
