import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { TeamResponse } from '../../../shared/models/team.model';

/**
 * TeamListComponent shows a paginated, searchable list of teams.
 * Managers and admins see a "Create Team" button.
 */
@Component({
  selector: 'app-team-list',
  standalone: false,
  templateUrl: './team-list.html',
  styleUrl: './team-list.scss',
})
export class TeamList implements OnInit {
  readonly teams = signal<TeamResponse[]>([]);
  readonly loading = signal(false);
  readonly totalElements = signal(0);

  page = 0;
  readonly pageSize = 20;

  constructor(
    private readonly teamService: TeamService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    if (this.authService.isTeamManager()) {
      this.loading.set(true);
      this.teamService.getMyTeams().subscribe({
        next: (teams) => {
          this.teams.set(teams);
          this.totalElements.set(teams.length);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    } else {
      this.load();
    }
  }

  load(): void {
    this.loading.set(true);
    const fetch$ = this.tenantService.currentOrgSlug()
      ? this.teamService.getByOrg(this.page, this.pageSize)
      : this.teamService.getAll(this.page, this.pageSize);

    fetch$.subscribe({
      next: (page) => {
        this.teams.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onPageChange(newPage: number): void {
    this.page = newPage;
    this.load();
  }

  openTeam(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', id]);
  }

  createTeam(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'teams', 'new']);
  }
}
