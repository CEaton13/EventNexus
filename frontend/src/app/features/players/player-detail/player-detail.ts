import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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

  readonly displayedColumns = ['tournament', 'wins', 'losses', 'mvp'];

  private playerId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
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
}
