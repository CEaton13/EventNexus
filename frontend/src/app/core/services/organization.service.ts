import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Organization } from '../../shared/models/organization.model';

/** Request body for creating a new organization. */
export interface CreateOrganizationRequest {
  name: string;
  slug: string;
  contactEmail: string;
}

/** Request body for adding a member to an organization. */
export interface AddMemberRequest {
  userId: number;
  orgRole: 'ORG_ADMIN' | 'ORG_MEMBER';
}

/** Member entry returned by member-list endpoints. */
export interface OrganizationMemberResponse {
  organizationId: number;
  organizationName: string;
  organizationSlug: string;
  userId: number;
  username: string;
  orgRole: 'ORG_ADMIN' | 'ORG_MEMBER';
  joinedAt: string;
}

/**
 * OrganizationService provides Angular HttpClient wrappers around
 * the backend `/api/organizations` endpoints.
 */
@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private readonly apiBase = '/api/organizations';

  constructor(private readonly http: HttpClient) {}

  /**
   * Creates a new organization. Requires TOURNAMENT_ADMIN role.
   *
   * @param request - name, slug, and contactEmail for the new org.
   * @returns Observable of the created Organization.
   */
  createOrganization(request: CreateOrganizationRequest): Observable<Organization> {
    return this.http.post<Organization>(this.apiBase, request);
  }

  /**
   * Returns all organizations. Requires TOURNAMENT_ADMIN role.
   *
   * @returns Observable of Organization array.
   */
  getAll(): Observable<Organization[]> {
    return this.http.get<Organization[]>(this.apiBase);
  }

  /**
   * Returns a single organization by slug. Public endpoint.
   *
   * @param slug - the org's URL-safe identifier.
   * @returns Observable of the matching Organization.
   */
  getBySlug(slug: string): Observable<Organization> {
    return this.http.get<Organization>(`${this.apiBase}/${slug}`);
  }

  /**
   * Returns all members of the given organization.
   *
   * @param orgId - the organization's primary key.
   * @returns Observable of OrganizationMemberResponse array.
   */
  getMembers(orgId: number): Observable<OrganizationMemberResponse[]> {
    return this.http.get<OrganizationMemberResponse[]>(`${this.apiBase}/${orgId}/members`);
  }

  /**
   * Adds a user to an organization. Requires TOURNAMENT_ADMIN or ORG_ADMIN.
   *
   * @param orgId   - the organization's primary key.
   * @param request - userId and orgRole for the new member.
   * @returns Observable of the new OrganizationMemberResponse.
   */
  addMember(orgId: number, request: AddMemberRequest): Observable<OrganizationMemberResponse> {
    return this.http.post<OrganizationMemberResponse>(
      `${this.apiBase}/${orgId}/members`,
      request
    );
  }

  /**
   * Removes a user from an organization. Requires ORG_ADMIN.
   *
   * @param orgId  - the organization's primary key.
   * @param userId - the user to remove.
   * @returns Observable<void>.
   */
  removeMember(orgId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/${orgId}/members/${userId}`);
  }
}
