import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, finalize, tap, catchError, firstValueFrom } from 'rxjs';
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
  refreshToken: string;
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
 * XSS risk. The refresh token is stored in sessionStorage so the session
 * can be restored on page reload within the same browser tab.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiBase = '/api/auth';
  private static readonly REFRESH_TOKEN_KEY = 'refresh_token';

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
      .pipe(tap((res) => this.handleAuthResponse(res)));
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
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  /**
   * Requests a new access token using the stored refresh token.
   * Sends the refresh token in the request body.
   *
   * @returns Observable of AuthResponse with fresh access token.
   */
  refreshToken(): Observable<AuthResponse> {
    const token = sessionStorage.getItem(AuthService.REFRESH_TOKEN_KEY);
    return this.http
      .post<AuthResponse>(`${this.apiBase}/refresh`, { refreshToken: token })
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  /**
   * Logs the user out: invalidates the refresh token on the server and clears
   * in-memory state. State is always cleared regardless of whether the HTTP
   * call succeeds, so the user is never left in a broken authenticated state.
   *
   * @returns Observable<void>.
   */
  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiBase}/logout`, {})
      .pipe(finalize(() => this.clearSession()));
  }

  /**
   * Clears in-memory auth state without making an HTTP call.
   * Used by the auth interceptor when the refresh token has expired.
   */
  clearSession(): void {
    this.accessToken = null;
    this.user.set(null);
    sessionStorage.removeItem(AuthService.REFRESH_TOKEN_KEY);
    this.tenantService.clearTenant();
  }

  /**
   * Attempts to restore the session on app startup by using the stored
   * refresh token. Resolves silently if no token exists or if the refresh
   * fails (e.g. token expired).
   *
   * Called via APP_INITIALIZER so the user signal is populated before
   * any route guard or component runs.
   *
   * @returns Promise that resolves when the restore attempt is complete.
   */
  attemptSessionRestore(): Promise<void> {
    const token = sessionStorage.getItem(AuthService.REFRESH_TOKEN_KEY);
    if (!token) return Promise.resolve();

    return firstValueFrom(
      this.refreshToken().pipe(
        catchError(() => {
          this.clearSession();
          return of(null);
        })
      )
    ).then(() => undefined);
  }

  /** Stores the tokens and user received from any auth endpoint. */
  private handleAuthResponse(res: AuthResponse): void {
    this.accessToken = res.accessToken;
    if (res.refreshToken) {
      sessionStorage.setItem(AuthService.REFRESH_TOKEN_KEY, res.refreshToken);
    }
    this.user.set(res.user);
    this.tenantService.setMemberships(res.organizations ?? []);
  }
}
