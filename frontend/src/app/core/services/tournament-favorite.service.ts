import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TournamentFavoriteResponse, FavoriteStatus } from '../../shared/models/follow.model';

/**
 * Handles all HTTP calls for tournament favorite/unfavorite and the "My Tournaments" list.
 * Endpoints require authentication (JWT is attached by the auth interceptor).
 */
@Injectable({ providedIn: 'root' })
export class TournamentFavoriteService {
  private readonly base = '/api';

  constructor(private readonly http: HttpClient) {}

  /**
   * Favorites a tournament. Returns the created favorite record.
   */
  favorite(tournamentId: number): Observable<TournamentFavoriteResponse> {
    return this.http.post<TournamentFavoriteResponse>(
      `${this.base}/tournaments/${tournamentId}/favorite`, {});
  }

  /**
   * Removes a tournament from favorites.
   */
  unfavorite(tournamentId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/tournaments/${tournamentId}/favorite`);
  }

  /**
   * Returns whether the authenticated user has favorited the given tournament.
   */
  isFavorited(tournamentId: number): Observable<FavoriteStatus> {
    return this.http.get<FavoriteStatus>(
      `${this.base}/tournaments/${tournamentId}/favorite/status`);
  }

  /**
   * Returns all tournaments favorited by the authenticated user.
   */
  getMyFavorites(): Observable<TournamentFavoriteResponse[]> {
    return this.http.get<TournamentFavoriteResponse[]>(`${this.base}/users/me/favorites`);
  }
}
