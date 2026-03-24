import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';

/**
 * LoginComponent presents a Material form for username/password authentication.
 * On success, navigates to /:orgSlug/tournaments (or /:orgSlug/admin/dashboard for admins).
 */
@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  /** Submits credentials and navigates on success. */
  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: () => {
        const slug =
          this.tenantService.currentOrgSlug() ??
          this.tenantService.memberships()[0]?.organizationSlug;
        if (slug) {
          const dest = this.authService.isAdmin()
            ? [slug, 'admin', 'dashboard']
            : [slug, 'tournaments'];
          this.router.navigate(dest);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message ?? 'Invalid username or password.';
      },
    });
  }
}
