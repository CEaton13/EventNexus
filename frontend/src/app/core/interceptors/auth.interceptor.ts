import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, filter, take, switchMap, catchError } from 'rxjs';
import { AuthService, AuthResponse } from '../services/auth';

/**
 * AuthInterceptor attaches the in-memory Bearer token to every outbound
 * HTTP request. On a 401 response it attempts a single token refresh and
 * retries the original request; if the refresh also fails, the user is
 * logged out.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshSubject = new BehaviorSubject<string | null>(null);

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
    if (this.isRefreshing) {
      return this.refreshSubject.pipe(
        filter((token): token is string => token !== null),
        take(1),
        switchMap(token => next.handle(this.addToken(request, token)))
      );
    }

    this.isRefreshing = true;
    this.refreshSubject.next(null);

    return this.authService.refreshToken().pipe(
      switchMap((res: AuthResponse) => {
        this.isRefreshing = false;
        this.refreshSubject.next(res.accessToken);
        return next.handle(this.addToken(request, res.accessToken));
      }),
      catchError(err => {
        this.isRefreshing = false;
        this.authService.logout().subscribe();
        return throwError(() => err);
      })
    );
  }
}