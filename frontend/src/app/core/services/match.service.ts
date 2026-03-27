import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MatchDetail, MatchResponse } from '../../shared/models/match.model';
import { TenantService } from './tenant.service';

/**
 * MatchService communicates with the `/api/matches` and org-scoped tournament match endpoints.
 */
@Injectable({ providedIn: 'root' })
export class MatchService {
  private readonly publicBase = '/api/matches';

  private get orgBase(): string {
    return `/api/orgs/${this.tenantService.currentOrgSlug()}/matches`;
  }

  constructor(
    private readonly http: HttpClient,
    private readonly tenantService: TenantService,
  ) {}

  /**
   * Schedules a match by setting time and venue.
   * @param id Match primary key.
   * @param scheduledTime ISO datetime string.
   * @param venueId Venue primary key.
   */
  schedule(id: number, scheduledTime: string, venueId: number): Observable<MatchResponse> {
    return this.http.patch<MatchResponse>(`${this.orgBase}/${id}/schedule`, {
      scheduledTime,
      venueId,
    });
  }

  /**
   * Records the winner of a completed match.
   * @param id Match primary key.
   * @param winnerId Winning team's primary key.
   */
  recordResult(id: number, winnerId: number): Observable<MatchResponse> {
    return this.http.patch<MatchResponse>(`${this.orgBase}/${id}/result`, { winnerId });
  }

  /**
   * Returns full detail for a single match by its primary key.
   * This is a public endpoint — no organisation slug is required.
   * @param id Match primary key.
   */
  getById(id: number): Observable<MatchDetail> {
    return this.http.get<MatchDetail>(`${this.publicBase}/${id}`);
  }

  /**
   * Returns all matches for a tournament.
   * @param tournamentId Tournament primary key.
   */
  getByTournament(tournamentId: number): Observable<MatchResponse[]> {
    return this.http.get<MatchResponse[]>(
      `/api/orgs/${this.tenantService.currentOrgSlug()}/tournaments/${tournamentId}/matches`,
    );
  }
}
