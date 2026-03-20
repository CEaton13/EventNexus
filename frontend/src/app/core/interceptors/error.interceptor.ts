import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * ErrorInterceptor provides global HTTP error handling:
 * - 403 Forbidden → redirects to /unauthorized
 * - 500 Server Error → displays a snackbar notification
 *
 * All other errors are passed through to the caller.
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  /** @inheritdoc */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse) {
          if (error.status === 403) {
            this.router.navigate(['/unauthorized']);
          } else if (error.status >= 500) {
            this.snackBar.open(
              'A server error occurred. Please try again later.',
              'Dismiss',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          }
        }
        return throwError(() => error);
      })
    );
  }
}