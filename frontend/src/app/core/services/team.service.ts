import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TeamResponse, TeamRequest } from '../../shared/models/team.model';
import { PlayerResponse } from '../../shared/models/player.model';
import { PageResponse, TournamentSummary } from '../../shared/models/tournament.model';

/**
 * TeamService communicates with the `/api/teams` endpoints.
 */
@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly base = '/api/teams';

  constructor(private readonly http: HttpClient) {}

  /**
   * Returns a paginated list of all teams.
   * @param page Zero-based page index.
   * @param size Page size.
   */
  getAll(page = 0, size = 20): Observable<PageResponse<TeamResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<TeamResponse>>(this.base, { params });
  }

  /**
   * Returns a single team by ID including active player count.
   * @param id Team primary key.
   */
  getById(id: number): Observable<TeamResponse> {
    return this.http.get<TeamResponse>(`${this.base}/${id}`);
  }

  /**
   * Returns all players on a team.
   * @param id Team primary key.
   */
  getPlayers(id: number): Observable<PlayerResponse[]> {
    return this.http.get<PlayerResponse[]>(`${this.base}/${id}/players`);
  }

  /**
   * Creates a new team. The authenticated user becomes the manager.
   * @param data Team creation payload.
   */
  create(data: TeamRequest): Observable<TeamResponse> {
    return this.http.post<TeamResponse>(this.base, data);
  }

  /**
   * Updates an existing team's details.
   * @param id Team primary key.
   * @param data Updated values.
   */
  update(id: number, data: TeamRequest): Observable<TeamResponse> {
    return this.http.put<TeamResponse>(`${this.base}/${id}`, data);
  }

  /**
   * Deletes a team by ID.
   * @param id Team primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /**
   * Returns all teams managed by the currently authenticated user.
   */
  getMyTeams(): Observable<TeamResponse[]> {
    return this.http.get<TeamResponse[]>(`${this.base}/mine`);
  }

  /**
   * Returns all tournaments a team is registered for.
   * @param id Team primary key.
   */
  getTournaments(id: number): Observable<TournamentSummary[]> {
    return this.http.get<TournamentSummary[]>(`${this.base}/${id}/tournaments`);
  }
}
