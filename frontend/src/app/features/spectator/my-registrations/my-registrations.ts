import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MyRegistrationsService } from '../../../core/services/my-registrations.service';
import { MyRegistrationResponse } from '../../../shared/models/registration.model';

/**
 * Displays all tournament registrations for teams managed by the authenticated user.
 * Each row links to the public tournament hub (/t/:id).
 */
@Component({
  selector: 'app-my-registrations',
  standalone: false,
  templateUrl: './my-registrations.html',
  styleUrl: './my-registrations.scss',
})
export class MyRegistrations implements OnInit {
  readonly registrations = signal<MyRegistrationResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(
    private readonly myRegistrationsService: MyRegistrationsService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.myRegistrationsService.getMyRegistrations().subscribe({
      next: data => {
        this.registrations.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load registrations. Please try again.');
        this.loading.set(false);
      },
    });
  }

  goToTournament(tournamentId: number): void {
    this.router.navigate(['/t', tournamentId]);
  }

  statusColor(status: string): string {
    switch (status) {
      case 'APPROVED': return 'primary';
      case 'PENDING': return 'accent';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }
}
