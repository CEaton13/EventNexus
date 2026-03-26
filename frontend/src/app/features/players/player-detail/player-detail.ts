import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { PlayerService } from '../../../core/services/player.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { PlayerResponse, PlayerStatsResponse } from '../../../shared/models/player.model';

/**
 * PlayerDetailComponent displays a player's profile and per-tournament stats.
 */
@Component({
  selector: 'app-player-detail',
  standalone: false,
  templateUrl: './player-detail.html',
  styleUrl: './player-detail.scss',
})
export class PlayerDetail implements OnInit {
  readonly player = signal<PlayerResponse | null>(null);
  readonly stats = signal<PlayerStatsResponse[]>([]);
  readonly loading = signal(false);

  readonly displayedColumns = ['tournament', 'wins', 'losses', 'winRate', 'mvp'];

  readonly totalWins = computed(() => this.stats().reduce((sum, s) => sum + s.wins, 0));

  readonly totalLosses = computed(() => this.stats().reduce((sum, s) => sum + s.losses, 0));

  readonly totalMvps = computed(() => this.stats().reduce((sum, s) => sum + (s.mvpCount ?? 0), 0));

  readonly winRate = computed(() => {
    const total = this.totalWins() + this.totalLosses();
    if (total === 0) return 0;
    return Math.round((this.totalWins() / total) * 100);
  });

  readonly tournamentsPlayed = computed(() => this.stats().length);

  private playerId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly playerService: PlayerService,
    readonly authService: AuthService,
    readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.playerId = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.playerService.getById(this.playerId).subscribe({
      next: (p) => {
        this.player.set(p);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.playerService.getStats(this.playerId).subscribe((s) => this.stats.set(s));
  }

  canEdit(): boolean {
    const p = this.player();
    if (!p) return false;
    return this.authService.isAdmin() || this.authService.isTeamManager();
  }

  editPlayer(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'players', this.playerId, 'edit']);
  }

  /**
   * Opens a confirmation dialog before removing a player from their team.
   * Navigates back to the team page on successful deletion.
   */
  deletePlayer(): void {
    const p = this.player();
    if (!p) return;
    this.dialog
      .open(ConfirmDialog, {
        panelClass: 'dark-dialog',
        data: {
          title: 'Remove Player',
          message: `Remove "${p.gamerTag}" from ${p.teamName}? This cannot be undone.`,
          confirmLabel: 'Remove',
          cancelLabel: 'Cancel',
        },
      })
      .afterClosed()
      .subscribe((confirmed: boolean) => {
        if (!confirmed) return;
        this.playerService.delete(this.playerId).subscribe({
          next: () => {
            this.snackBar.open('Player removed', 'OK', { duration: 3000 });
            this.router.navigate([
              this.tenantService.currentOrgSlug(),
              'teams',
              this.player()!.teamId,
            ]);
          },
        });
      });
  }
}
