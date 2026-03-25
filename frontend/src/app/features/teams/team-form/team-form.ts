import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamService } from '../../../core/services/team.service';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * TeamFormComponent handles both create (no :id) and edit (:id in route) flows
 * for team management.
 */
@Component({
  selector: 'app-team-form',
  standalone: false,
  templateUrl: './team-form.html',
  styleUrl: './team-form.scss',
})
export class TeamForm implements OnInit {
  form!: FormGroup;
  readonly loading = signal(false);
  readonly isEdit = signal(false);
  private teamId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly teamService: TeamService,
    private readonly tenantService: TenantService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      tag: ['', [Validators.required, Validators.maxLength(10)]],
      logoUrl: ['', Validators.maxLength(500)],
      homeRegion: ['', Validators.maxLength(100)],
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.teamId = Number(id);
      this.teamService.getById(this.teamId).subscribe(t => this.form.patchValue(t));
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const data = this.form.value;
    const op = this.isEdit()
      ? this.teamService.update(this.teamId!, data)
      : this.teamService.create(data);

    op.subscribe({
      next: t => {
        this.snackBar.open(this.isEdit() ? 'Team updated' : 'Team created', 'OK', { duration: 3000 });
        this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', t.id]);
      },
      error: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams']);
  }
}
