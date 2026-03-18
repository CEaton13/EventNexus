import { Injectable, signal, computed } from '@angular/core';

/** Minimal user shape returned from the API. */
export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: 'TOURNAMENT_ADMIN' | 'TEAM_MANAGER' | 'SPECTATOR';
}

/**
 * AuthService — stub for Day 20.
 *
 * Holds reactive auth state via Angular Signals.
 * Full implementation (HTTP calls, token storage, interceptor wiring)
 * is completed on Day 21.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  /** Currently authenticated user, or null when logged out. */
  readonly user = signal<UserResponse | null>(null);

  /** True when a user is signed in. */
  readonly isAuthenticated = computed(() => this.user() !== null);

  /** True when the signed-in user has the TOURNAMENT_ADMIN role. */
  readonly isAdmin = computed(() => this.user()?.role === 'TOURNAMENT_ADMIN');

  /** True when the signed-in user has the TEAM_MANAGER role. */
  readonly isTeamManager = computed(() => this.user()?.role === 'TEAM_MANAGER');
}
