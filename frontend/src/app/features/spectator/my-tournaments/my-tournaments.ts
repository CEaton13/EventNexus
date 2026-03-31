import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TournamentFavoriteService } from '../../../core/services/tournament-favorite.service';
import { TournamentFavoriteResponse } from '../../../shared/models/follow.model';

/**
 * Displays tournaments the authenticated user has favorited (watchlist).
 * Allows removing a tournament from the watchlist inline.
 */
@Component({
  selector: 'app-my-tournaments',
  standalone: false,
  templateUrl: './my-tournaments.html',
  styleUrl: './my-tournaments.scss',
})
export class MyTournaments implements OnInit {
  readonly favorites = signal<TournamentFavoriteResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor(
    private readonly tournamentFavoriteService: TournamentFavoriteService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.tournamentFavoriteService.getMyFavorites().subscribe({
      next: data => {
        this.favorites.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load favorites. Please try again.');
        this.loading.set(false);
      },
    });
  }

  goToTournament(tournamentId: number): void {
    this.router.navigate(['/t', tournamentId]);
  }

  removeFavorite(event: Event, tournamentId: number): void {
    event.stopPropagation();
    this.tournamentFavoriteService.unfavorite(tournamentId).subscribe({
      next: () => {
        this.favorites.update(list => list.filter(f => f.tournamentId !== tournamentId));
        this.snackBar.open('Removed from favorites', 'Dismiss', { duration: 2500 });
      },
      error: () => this.snackBar.open('Failed to remove favorite', 'Dismiss', { duration: 3000 }),
    });
  }
}
