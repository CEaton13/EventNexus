import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PlayerService } from '../../../core/services/player.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * PlayerFormComponent handles create (/teams/:teamId/players/new)
 * and edit (/players/:id/edit) flows.
 */
@Component({
  selector: 'app-player-form',
  standalone: false,
  templateUrl: './player-form.html',
  styleUrl: './player-form.scss',
})
export class PlayerForm implements OnInit {
  form!: FormGroup;
  readonly loading = signal(false);
  readonly isEdit = signal(false);
  private playerId?: number;
  private teamId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly playerService: PlayerService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      gamerTag: ['', [Validators.required, Validators.maxLength(50)]],
      realName: ['', Validators.maxLength(100)],
      position: ['', Validators.maxLength(50)],
      country: ['', Validators.maxLength(60)],
      avatarUrl: ['', Validators.maxLength(500)],
    });

    const pid = this.route.snapshot.paramMap.get('playerId') || this.route.snapshot.paramMap.get('id');
    const tid = this.route.snapshot.paramMap.get('teamId');
    if (pid) {
      this.isEdit.set(true);
      this.playerId = Number(pid);
      this.playerService.getById(this.playerId).subscribe(p => this.form.patchValue(p));
    }
    if (tid) this.teamId = Number(tid);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const data = this.form.value;
    const op = this.isEdit()
      ? this.playerService.update(this.playerId!, data)
      : this.playerService.create(this.teamId!, data);

    op.subscribe({
      next: p => {
        this.snackBar.open(this.isEdit() ? 'Player updated' : 'Player added', 'OK', { duration: 3000 });
        this.router.navigate([this.tenantService.currentOrgSlug(), 'players', p.id]);
      },
      error: () => this.loading.set(false),
    });
  }

  cancel(): void {
    history.back();
  }
}
