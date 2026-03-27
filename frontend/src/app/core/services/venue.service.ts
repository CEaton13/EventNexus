import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from './tenant.service';
import { VenueRequest, VenueResponse } from '../../shared/models/venue.model';

/**
 * VenueService communicates with the org-scoped `/api/orgs/{slug}/venues` endpoints.
 * All URLs are prefixed with the active organization's slug.
 */
@Injectable({ providedIn: 'root' })
export class VenueService {
  constructor(
    private readonly http: HttpClient,
    private readonly tenantService: TenantService,
  ) {}

  private get base(): string {
    return `/api/orgs/${this.tenantService.currentOrgSlug()}/venues`;
  }

  /**
   * Returns all venues for the active organization.
   */
  getAll(): Observable<VenueResponse[]> {
    return this.http.get<VenueResponse[]>(this.base);
  }

  /**
   * Returns a single venue with utilization stats.
   * @param id Venue primary key.
   */
  getById(id: number): Observable<VenueResponse> {
    return this.http.get<VenueResponse>(`${this.base}/${id}`);
  }

  /**
   * Creates a new venue. Requires TOURNAMENT_ADMIN.
   * @param data Venue creation payload.
   */
  create(data: VenueRequest): Observable<VenueResponse> {
    return this.http.post<VenueResponse>(this.base, data);
  }

  /**
   * Updates an existing venue. Requires TOURNAMENT_ADMIN.
   * @param id Venue primary key.
   * @param data Updated values.
   */
  update(id: number, data: VenueRequest): Observable<VenueResponse> {
    return this.http.put<VenueResponse>(`${this.base}/${id}`, data);
  }

  /**
   * Deletes a venue. Requires TOURNAMENT_ADMIN.
   * @param id Venue primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
