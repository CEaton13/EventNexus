import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerResponse, PlayerStatsResponse, PlayerRequest } from '../../shared/models/player.model';

/**
 * PlayerService communicates with the `/api/players` and `/api/teams/{id}/players` endpoints.
 */
@Injectable({ providedIn: 'root' })
export class PlayerService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Returns a single player profile.
   * @param id Player primary key.
   */
  getById(id: number): Observable<PlayerResponse> {
    return this.http.get<PlayerResponse>(`/api/players/${id}`);
  }

  /**
   * Returns all per-tournament stats for a player.
   * @param id Player primary key.
   */
  getStats(id: number): Observable<PlayerStatsResponse[]> {
    return this.http.get<PlayerStatsResponse[]>(`/api/players/${id}/stats`);
  }

  /**
   * Adds a new player to a team.
   * @param teamId Team primary key.
   * @param data Player creation payload.
   */
  create(teamId: number, data: PlayerRequest): Observable<PlayerResponse> {
    return this.http.post<PlayerResponse>(`/api/teams/${teamId}/players`, data);
  }

  /**
   * Updates a player's profile.
   * @param id Player primary key.
   * @param data Updated values.
   */
  update(id: number, data: PlayerRequest): Observable<PlayerResponse> {
    return this.http.put<PlayerResponse>(`/api/players/${id}`, data);
  }

  /**
   * Soft-deletes a player (sets active = false).
   * @param id Player primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/players/${id}`);
  }
}
