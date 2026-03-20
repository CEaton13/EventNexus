import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { OrganizationMembership } from '../../shared/models/organization.model';
import { TenantService } from './tenant.service';

/** User shape returned by the API. */
export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: 'TOURNAMENT_ADMIN' | 'TEAM_MANAGER' | 'SPECTATOR';
}

/** Payload returned by login and register endpoints. */
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
  organizations: OrganizationMembership[];
}

/** Credentials sent to POST /api/auth/login. */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Payload sent to POST /api/auth/register. */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: string;
}

/**
 * AuthService manages authentication state using Angular Signals
 * and communicates with the backend auth endpoints.
 *
 * Access token is stored in-memory only (not localStorage) to reduce
 * XSS risk. The refresh token is stored as an httpOnly cookie by the server.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiBase = '/api/auth';

  /** In-memory access token — cleared on page reload (refresh flow handles re-auth). */
  private accessToken: string | null = null;

  /** Currently authenticated user, or null when logged out. */
  readonly user = signal<UserResponse | null>(null);

  /** True when a user is signed in. */
  readonly isAuthenticated = computed(() => this.user() !== null);

  /** True when the signed-in user has the TOURNAMENT_ADMIN role. */
  readonly isAdmin = computed(() => this.user()?.role === 'TOURNAMENT_ADMIN');

  /** True when the signed-in user has the TEAM_MANAGER role. */
  readonly isTeamManager = computed(() => this.user()?.role === 'TEAM_MANAGER');

  constructor(
    private readonly http: HttpClient,
    private readonly tenantService: TenantService,
  ) {}

  /**
   * Returns the current in-memory access token, or null if not authenticated.
   * Used by the auth interceptor to attach the Authorization header.
   */
  getAccessToken(): string | null {
    return this.accessToken;
  }

  /**
   * Authenticates the user and stores the access token and user signal.
   *
   * @param credentials - username and password.
   * @returns Observable of AuthResponse.
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/login`, credentials)
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  /**
   * Registers a new user account.
   *
   * @param data - Registration payload.
   * @returns Observable of AuthResponse.
   */
  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/register`, data)
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  /**
   * Requests a new access token using the httpOnly refresh-token cookie.
   *
   * @returns Observable of AuthResponse with fresh access token.
   */
  refreshToken(): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/refresh`, {}, { withCredentials: true })
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  /**
   * Logs the user out: clears in-memory state and invalidates the refresh
   * token on the server.
   *
   * @returns Observable<void>.
   */
  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiBase}/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => {
          this.accessToken = null;
          this.user.set(null);
          this.tenantService.clearTenant();
        })
      );
  }

  /** Stores the token and user received from any auth endpoint. */
  private handleAuthResponse(res: AuthResponse): void {
    this.accessToken = res.accessToken;
    this.user.set(res.user);
    this.tenantService.setMemberships(res.organizations ?? []);
  }
}
