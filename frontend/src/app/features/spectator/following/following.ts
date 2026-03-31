import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamFollowService } from '../../../core/services/team-follow.service';
import { TeamFollowResponse } from '../../../shared/models/follow.model';

/**
 * Displays all teams the authenticated user is following.
 * Allows unfollowing a team inline.
 */
@Component({
  selector: 'app-following',
  standalone: false,
  templateUrl: './following.html',
  styleUrl: './following.scss',
})
export class Following implements OnInit {
  readonly follows = signal<TeamFollowResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(
    private readonly teamFollowService: TeamFollowService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.teamFollowService.getMyFollows().subscribe({
      next: data => {
        this.follows.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load followed teams. Please try again.');
        this.loading.set(false);
      },
    });
  }

  goToTeam(teamId: number): void {
    this.router.navigate(['/teams', teamId]);
  }

  unfollow(event: Event, teamId: number): void {
    event.stopPropagation();
    this.teamFollowService.unfollow(teamId).subscribe({
      next: () => {
        this.follows.update(list => list.filter(f => f.teamId !== teamId));
        this.snackBar.open('Unfollowed team', 'Dismiss', { duration: 2500 });
      },
      error: () => this.snackBar.open('Failed to unfollow team', 'Dismiss', { duration: 3000 }),
    });
  }
}
