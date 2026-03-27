import { Component, Inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';

export interface AuthDialogData {
  /** 0 = Sign In tab, 1 = Create Account tab */
  initialTab?: number;
}

/**
 * AuthDialog presents login and registration in a two-tab modal.
 *
 * On success the dialog closes with `true` so callers can continue
 * the action that required authentication (e.g. team registration).
 * On cancel/dismiss it closes with `false`.
 */
@Component({
  selector: 'app-auth-dialog',
  standalone: false,
  templateUrl: './auth-dialog.html',
  styleUrl: './auth-dialog.scss',
})
export class AuthDialog {
  // Initialized in constructor so @Inject(MAT_DIALOG_DATA) data is available first.
  readonly selectedTab: ReturnType<typeof signal<number>>;

  // ── Login form ─────────────────────────────────────────────────────────────
  readonly loginForm: FormGroup;
  readonly loginLoading = signal(false);
  readonly loginError = signal('');

  // ── Register form ──────────────────────────────────────────────────────────
  readonly registerForm: FormGroup;
  readonly registerLoading = signal(false);
  readonly registerError = signal('');

  readonly roleOptions = [
    { value: 'SPECTATOR',        label: 'Spectator — Follow tournaments' },
    { value: 'TEAM_MANAGER',     label: 'Team Manager — Manage a roster' },
    { value: 'TOURNAMENT_ADMIN', label: 'Organizer — Run events' },
  ];

  constructor(
    readonly dialogRef: MatDialogRef<AuthDialog>,
    @Inject(MAT_DIALOG_DATA) private readonly data: AuthDialogData,
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly tenantService: TenantService,
  ) {
    this.selectedTab = signal(this.data?.initialTab ?? 0);

    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role:     ['SPECTATOR', Validators.required],
    });
  }

  onLogin(): void {
    if (this.loginForm.invalid) return;
    this.loginLoading.set(true);
    this.loginError.set('');

    this.authService.login(this.loginForm.value).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.loginLoading.set(false);
        this.loginError.set(err?.error?.message ?? 'Invalid username or password.');
      },
    });
  }

  onRegister(): void {
    if (this.registerForm.invalid) return;
    this.registerLoading.set(true);
    this.registerError.set('');

    this.authService.register(this.registerForm.value).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        this.registerLoading.set(false);
        this.registerError.set(err?.error?.message ?? 'Registration failed. Please try again.');
      },
    });
  }

  close(): void {
    this.dialogRef.close(false);
  }
}
