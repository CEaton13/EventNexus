import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';

interface OrganizationResponse {
  id: number;
  name: string;
  slug: string;
  contactEmail: string;
  createdAt: string;
}

/**
 * CreateOrgComponent allows a TOURNAMENT_ADMIN to create their first organization.
 * After creation it refreshes the auth session (so TenantService picks up the new
 * membership) and then navigates to the new org's admin dashboard.
 */
@Component({
  selector: 'app-create-org',
  standalone: false,
  templateUrl: './create-org.html',
  styleUrl: './create-org.scss',
})
export class CreateOrg {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly http: HttpClient,
    private readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      slug: [
        '',
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(/^[a-z0-9-]+$/),
        ],
      ],
      contactEmail: ['', [Validators.required, Validators.email]],
    });
  }

  /** Submits the org creation form, refreshes auth session, then navigates to admin dashboard. */
  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.http.post<OrganizationResponse>('/api/organizations', this.form.value).subscribe({
      next: (org) => {
        // Refresh the auth session so TenantService receives the new org membership.
        this.authService.refreshToken().subscribe({
          next: () => {
            this.router.navigate([org.slug, 'admin', 'dashboard']);
          },
          error: () => {
            // Refresh failed but org was created — navigate anyway; guard will re-auth if needed.
            this.router.navigate([org.slug, 'admin', 'dashboard']);
          },
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message ?? 'Failed to create organization. Please try again.';
      },
    });
  }

  /** Generates a URL-safe slug from the org name as the user types. */
  autoSlug(): void {
    const name: string = this.form.get('name')?.value ?? '';
    const slug = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
    this.form.get('slug')?.setValue(slug, { emitEvent: false });
  }
}
