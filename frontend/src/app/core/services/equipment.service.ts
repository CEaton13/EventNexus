import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from './tenant.service';
import {
  EquipmentRequest,
  EquipmentResponse,
  LoadoutRequest,
  LoadoutResponse,
} from '../../shared/models/equipment.model';

/**
 * EquipmentService communicates with the org-scoped `/api/orgs/{slug}/equipment` endpoints
 * and tournament-scoped `/api/orgs/{slug}/tournaments/{id}/loadouts` endpoints.
 * All URLs are prefixed with the active organization's slug.
 */
@Injectable({ providedIn: 'root' })
export class EquipmentService {
  constructor(
    private readonly http: HttpClient,
    private readonly tenantService: TenantService,
  ) {}

  private get base(): string {
    return `/api/orgs/${this.tenantService.currentOrgSlug()}/equipment`;
  }

  private tournamentsBase(): string {
    return `/api/orgs/${this.tenantService.currentOrgSlug()}/tournaments`;
  }

  /**
   * Returns all equipment for the active organization.
   */
  getAll(): Observable<EquipmentResponse[]> {
    return this.http.get<EquipmentResponse[]>(this.base);
  }

  /**
   * Returns a single piece of equipment by ID.
   * @param id Equipment primary key.
   */
  getById(id: number): Observable<EquipmentResponse> {
    return this.http.get<EquipmentResponse>(`${this.base}/${id}`);
  }

  /**
   * Returns all equipment assigned to a specific venue.
   * @param venueId Venue primary key.
   */
  getByVenue(venueId: number): Observable<EquipmentResponse[]> {
    return this.http.get<EquipmentResponse[]>(
      `/api/orgs/${this.tenantService.currentOrgSlug()}/venues/${venueId}/equipment`,
    );
  }

  /**
   * Creates a new piece of equipment. Requires TOURNAMENT_ADMIN.
   * @param req Equipment creation payload.
   */
  create(req: EquipmentRequest): Observable<EquipmentResponse> {
    return this.http.post<EquipmentResponse>(this.base, req);
  }

  /**
   * Updates an existing piece of equipment. Requires TOURNAMENT_ADMIN.
   * @param id Equipment primary key.
   * @param req Updated values.
   */
  update(id: number, req: EquipmentRequest): Observable<EquipmentResponse> {
    return this.http.put<EquipmentResponse>(`${this.base}/${id}`, req);
  }

  /**
   * Deletes a piece of equipment. Requires TOURNAMENT_ADMIN.
   * @param id Equipment primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /**
   * Returns all equipment loadouts for a tournament.
   * @param tournamentId Tournament primary key.
   */
  getLoadouts(tournamentId: number): Observable<LoadoutResponse[]> {
    return this.http.get<LoadoutResponse[]>(
      `${this.tournamentsBase()}/${tournamentId}/loadouts`,
    );
  }

  /**
   * Assigns equipment to a team within a tournament.
   * @param tournamentId Tournament primary key.
   * @param req Loadout assignment payload.
   */
  assignLoadout(tournamentId: number, req: LoadoutRequest): Observable<LoadoutResponse> {
    return this.http.post<LoadoutResponse>(
      `${this.tournamentsBase()}/${tournamentId}/loadouts`,
      req,
    );
  }

  /**
   * Removes (returns) an equipment loadout assignment.
   * @param tournamentId Tournament primary key.
   * @param loadoutId Loadout primary key.
   */
  removeLoadout(tournamentId: number, loadoutId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.tournamentsBase()}/${tournamentId}/loadouts/${loadoutId}`,
    );
  }
}
