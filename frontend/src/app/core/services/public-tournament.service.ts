import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  TournamentDetail,
  TournamentSummary,
  PageResponse,
  RegistrationResponse,
  StandingsResponse,
} from '../../shared/models/tournament.model';
import { BracketResponse } from '../../shared/models/match.model';

/**
 * PublicTournamentService provides org-agnostic, unauthenticated access
 * to tournament data for the public tournament hub at /t/:id.
 *
 * Unlike TournamentService, this service does NOT prefix URLs with the
 * active org slug via TenantService. It targets the new
 * PublicTournamentController endpoints at /api/tournaments/{id}/...
 */
@Injectable({
  providedIn: 'root',
})
export class PublicTournamentService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/tournaments';

  /**
   * Fetches a paginated list of all public tournaments across all organizations.
   * Used by the public tournament browser at /tournaments (no auth required).
   *
   * @param page zero-based page index
   * @param size page size
   */
  getAll(page = 0, size = 20): Observable<PageResponse<TournamentSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<TournamentSummary>>(this.base, { params });
  }

  /**
   * Fetches full tournament details by ID.
   *
   * @param id tournament primary key
   */
  getById(id: number): Observable<TournamentDetail> {
    return this.http.get<TournamentDetail>(`${this.base}/${id}`);
  }

  /**
   * Fetches the bracket structure for a tournament.
   *
   * @param id tournament primary key
   */
  getBracket(id: number): Observable<BracketResponse> {
    return this.http.get<BracketResponse>(`${this.base}/${id}/bracket`);
  }

  /**
   * Fetches current standings for a tournament.
   *
   * @param id tournament primary key
   */
  getStandings(id: number): Observable<StandingsResponse[]> {
    return this.http.get<StandingsResponse[]>(`${this.base}/${id}/standings`);
  }

  /**
   * Fetches all registered teams for a tournament.
   *
   * @param id tournament primary key
   */
  getRegisteredTeams(id: number): Observable<RegistrationResponse[]> {
    return this.http.get<RegistrationResponse[]>(`${this.base}/${id}/teams`);
  }
}
