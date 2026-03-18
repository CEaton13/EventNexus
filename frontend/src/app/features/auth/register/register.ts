import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

/**
 * RegisterComponent presents a Material form for creating a new account.
 * After successful registration, navigates to the appropriate home page.
 */
@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  readonly roleOptions = [
    { value: 'TOURNAMENT_ADMIN', label: 'Tournament Admin' },
    { value: 'TEAM_MANAGER', label: 'Team Manager' },
    { value: 'SPECTATOR', label: 'Spectator' },
  ];

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['SPECTATOR', Validators.required],
    });
  }

  /** Submits the registration form and navigates on success. */
  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.form.value).subscribe({
      next: () => {
        const target = this.authService.isAdmin() ? '/admin/dashboard' : '/tournaments';
        this.router.navigate([target]);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message ?? 'Registration failed. Please try again.';
      },
    });
  }
}
