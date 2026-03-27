import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from './tenant.service';
import {
  TournamentSummary,
  TournamentDetail,
  PageResponse,
  RegistrationResponse,
  StandingsResponse,
  TournamentCreateRequest,
  TournamentStatus,
  DashboardSummary,
} from '../../shared/models/tournament.model';
import { BracketResponse } from '../../shared/models/match.model';

/**
 * TournamentService communicates with all org-scoped tournament endpoints.
 * All URLs are prefixed with `/api/orgs/{orgSlug}/tournaments`.
 */
@Injectable({ providedIn: 'root' })
export class TournamentService {
  constructor(
    private readonly http: HttpClient,
    private readonly tenantService: TenantService,
  ) {}

  private get base(): string {
    return `/api/orgs/${this.tenantService.currentOrgSlug()}/tournaments`;
  }

  /**
   * Returns a paginated list of tournament summaries, optionally filtered.
   * @param page Zero-based page index.
   * @param size Page size.
   * @param status Optional status filter.
   * @param genreId Optional genre filter.
   */
  getAll(
    page = 0,
    size = 20,
    status?: string,
    genreId?: number,
  ): Observable<PageResponse<TournamentSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (genreId) params = params.set('genreId', genreId);
    return this.http.get<PageResponse<TournamentSummary>>(this.base, { params });
  }

  /**
   * Returns full tournament detail including genre theme and venue.
   * @param id Tournament primary key.
   */
  getById(id: number): Observable<TournamentDetail> {
    return this.http.get<TournamentDetail>(`${this.base}/${id}`);
  }

  /**
   * Creates a new tournament in DRAFT status.
   * @param data Tournament creation payload.
   */
  create(data: TournamentCreateRequest): Observable<TournamentDetail> {
    return this.http.post<TournamentDetail>(this.base, data);
  }

  /**
   * Updates mutable fields of an existing tournament.
   * @param id Tournament primary key.
   * @param data Updated values.
   */
  update(id: number, data: TournamentCreateRequest): Observable<TournamentDetail> {
    return this.http.put<TournamentDetail>(`${this.base}/${id}`, data);
  }

  /**
   * Deletes a DRAFT tournament.
   * @param id Tournament primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /**
   * Advances a tournament's lifecycle status.
   * @param id Tournament primary key.
   * @param status Target status.
   */
  updateStatus(id: number, status: TournamentStatus): Observable<TournamentDetail> {
    return this.http.patch<TournamentDetail>(`${this.base}/${id}/status`, { status });
  }

  /**
   * Returns the current standings for a tournament.
   * @param id Tournament primary key.
   */
  getStandings(id: number): Observable<StandingsResponse[]> {
    return this.http.get<StandingsResponse[]>(`${this.base}/${id}/standings`);
  }

  /**
   * Returns the full bracket structure for a tournament.
   * @param id Tournament primary key.
   */
  getBracket(id: number): Observable<BracketResponse> {
    return this.http.get<BracketResponse>(`${this.base}/${id}/bracket`);
  }

  /**
   * Returns registered teams for a tournament.
   * @param id Tournament primary key.
   */
  getRegisteredTeams(id: number): Observable<RegistrationResponse[]> {
    return this.http.get<RegistrationResponse[]>(`${this.base}/${id}/teams`);
  }

  /**
   * Registers a team for a tournament.
   * @param id Tournament primary key.
   * @param teamId Team primary key.
   */
  registerTeam(id: number, teamId: number): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.base}/${id}/register`, { teamId });
  }

  /**
   * Approves or rejects a team's registration.
   * @param id Tournament primary key.
   * @param teamId Team primary key.
   * @param status New registration status.
   */
  updateRegistrationStatus(
    id: number,
    teamId: number,
    status: string,
  ): Observable<RegistrationResponse> {
    return this.http.patch<RegistrationResponse>(`${this.base}/${id}/teams/${teamId}/status`, {
      status,
    });
  }

  /**
   * Returns aggregated admin dashboard metrics for the active organisation.
   */
  getDashboard(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(
      `/api/orgs/${this.tenantService.currentOrgSlug()}/admin/dashboard`,
    );
  }
}
