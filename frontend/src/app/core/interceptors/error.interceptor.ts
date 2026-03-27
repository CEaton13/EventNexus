import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * ErrorInterceptor provides global HTTP error handling:
 * - 403 Forbidden → shows snackbar
 * - 404 Not Found → shows snackbar
 * - 409 Conflict → shows snackbar with server message
 * - 5xx Server Error → shows snackbar
 *
 * 401 Unauthorized is intentionally NOT handled here — AuthInterceptor
 * intercepts 401s first to attempt a token refresh before giving up.
 *
 * All errors are also passed through so callers can handle them locally.
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private readonly snackBar: MatSnackBar) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse) {
          const message = (error.error as { message?: string })?.message;

          switch (error.status) {
            case 403:
              this.snackBar.open('You do not have permission to perform that action.', 'Dismiss', {
                duration: 5000,
                panelClass: 'snackbar-error',
              });
              break;
            case 404:
              this.snackBar.open(message ?? 'Resource not found.', 'Dismiss', {
                duration: 4000,
              });
              break;
            case 409:
              this.snackBar.open(
                message ?? 'Conflict — the action could not be completed.',
                'Dismiss',
                {
                  duration: 5000,
                  panelClass: 'snackbar-warn',
                },
              );
              break;
            default:
              if (error.status >= 500) {
                this.snackBar.open('A server error occurred. Please try again later.', 'Dismiss', {
                  duration: 5000,
                  panelClass: 'snackbar-error',
                });
              }
          }
        }
        return throwError(() => error);
      }),
    );
  }
}
