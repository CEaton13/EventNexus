import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MyRegistrationResponse } from '../../shared/models/registration.model';

/**
 * Fetches all tournament registrations for teams managed by the authenticated user.
 * Endpoint requires authentication (JWT is attached by the auth interceptor).
 */
@Injectable({ providedIn: 'root' })
export class MyRegistrationsService {
  private readonly base = '/api';

  constructor(private readonly http: HttpClient) {}

  /**
   * Returns all registrations across every team the current user manages,
   * ordered by tournament start date descending.
   */
  getMyRegistrations(): Observable<MyRegistrationResponse[]> {
    return this.http.get<MyRegistrationResponse[]>(`${this.base}/users/me/registrations`);
  }
}
