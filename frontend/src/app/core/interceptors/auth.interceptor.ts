import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, Subject, take, switchMap, catchError } from 'rxjs';
import { AuthService, AuthResponse } from '../services/auth';

/**
 * AuthInterceptor attaches the in-memory Bearer token to every outbound
 * HTTP request. On a 401 response it attempts a single token refresh and
 * retries the original request; if the refresh also fails, the session is
 * cleared and the error is rethrown for the ErrorInterceptor to redirect.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  /**
   * Broadcasts a fresh access token to requests that queued while a refresh
   * was in flight. Using Subject (not BehaviorSubject) so that calling
   * .error() on failure immediately unblocks all waiting subscribers instead
   * of leaving them hanging on a never-resolved filter.
   */
  private refreshSubject = new Subject<string>();

  constructor(private readonly authService: AuthService) {}

  /** @inheritdoc */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getAccessToken();
    const authedRequest = token ? this.addToken(request, token) : request;

    return next.handle(authedRequest).pipe(
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  private handle401(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Never attempt a refresh for auth endpoints — doing so creates an infinite
    // loop where logout/refresh 401s trigger more refresh attempts indefinitely.
    if (request.url.includes('/api/auth/')) {
      this.authService.clearSession();
      return throwError(() => new Error('Authentication failed'));
    }

    if (this.isRefreshing) {
      // Queue behind the in-flight refresh — will resolve or error with it.
      return this.refreshSubject.pipe(
        take(1),
        switchMap(token => next.handle(this.addToken(request, token)))
      );
    }

    this.isRefreshing = true;
    this.refreshSubject = new Subject<string>();

    return this.authService.refreshToken().pipe(
      switchMap((res: AuthResponse) => {
        this.isRefreshing = false;
        this.refreshSubject.next(res.accessToken);
        this.refreshSubject.complete();
        return next.handle(this.addToken(request, res.accessToken));
      }),
      catchError(err => {
        this.isRefreshing = false;
        // Error the subject so all queued requests fail immediately rather than hanging.
        this.refreshSubject.error(err);
        this.refreshSubject = new Subject<string>();
        // Clear session state without an HTTP call to avoid re-entering this path.
        this.authService.clearSession();
        return throwError(() => err);
      })
    );
  }
}
