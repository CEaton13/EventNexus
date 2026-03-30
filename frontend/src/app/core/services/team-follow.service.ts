import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TeamFollowResponse, FollowStatus } from '../../shared/models/follow.model';

/**
 * Handles all HTTP calls for team follow/unfollow and the "Following" list.
 * Endpoints require authentication (JWT is attached by the auth interceptor).
 */
@Injectable({ providedIn: 'root' })
export class TeamFollowService {
  private readonly base = '/api';

  constructor(private readonly http: HttpClient) {}

  /**
   * Follows a team. Returns the created follow record.
   */
  follow(teamId: number): Observable<TeamFollowResponse> {
    return this.http.post<TeamFollowResponse>(`${this.base}/teams/${teamId}/follow`, {});
  }

  /**
   * Unfollows a team.
   */
  unfollow(teamId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/teams/${teamId}/follow`);
  }

  /**
   * Returns whether the authenticated user is following the given team.
   */
  isFollowing(teamId: number): Observable<FollowStatus> {
    return this.http.get<FollowStatus>(`${this.base}/teams/${teamId}/follow/status`);
  }

  /**
   * Returns all teams followed by the authenticated user.
   */
  getMyFollows(): Observable<TeamFollowResponse[]> {
    return this.http.get<TeamFollowResponse[]>(`${this.base}/users/me/follows`);
  }
}
