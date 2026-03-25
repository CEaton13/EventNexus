import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VenueResponse } from '../../shared/models/venue.model';

/**
 * VenueService communicates with the `/api/venues` endpoints.
 */
@Injectable({ providedIn: 'root' })
export class VenueService {
  private readonly base = '/api/venues';

  constructor(private readonly http: HttpClient) {}

  /**
   * Returns all venues.
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
}
