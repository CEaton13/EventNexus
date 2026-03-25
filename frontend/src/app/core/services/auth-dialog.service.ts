import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, from, of, switchMap } from 'rxjs';
import { AuthService } from './auth';

/**
 * AuthDialogService provides a single entry point for triggering
 * authentication from anywhere in the app without importing MatDialog
 * or the AuthDialog component directly.
 *
 * Usage:
 * ```ts
 * this.authDialogService.requireAuth().subscribe(authenticated => {
 *   if (authenticated) { // proceed with the action }
 * });
 * ```
 */
@Injectable({
  providedIn: 'root',
})
export class AuthDialogService {
  private readonly dialog = inject(MatDialog);
  private readonly authService = inject(AuthService);

  /**
   * Opens the login tab of the auth dialog.
   * Returns an observable that emits `true` on successful login,
   * `false` if the dialog is dismissed.
   */
  openLogin(): Observable<boolean> {
    return this.openDialog(0);
  }

  /**
   * Opens the create account tab of the auth dialog.
   * Returns an observable that emits `true` on successful registration,
   * `false` if the dialog is dismissed.
   */
  openRegister(): Observable<boolean> {
    return this.openDialog(1);
  }

  /**
   * If the user is already authenticated, returns `of(true)` immediately.
   * Otherwise opens the login dialog and returns the result.
   */
  requireAuth(): Observable<boolean> {
    if (this.authService.isAuthenticated()) {
      return of(true);
    }
    return this.openLogin();
  }

  private openDialog(initialTab: 0 | 1): Observable<boolean> {
    return from(
      import('../../shared/components/auth-dialog/auth-dialog').then(m => m.AuthDialog)
    ).pipe(
      switchMap(AuthDialog => {
        const ref = this.dialog.open(AuthDialog, {
          width: '440px',
          panelClass: 'dark-dialog',
          disableClose: false,
          data: { initialTab },
        });
        return ref.afterClosed() as Observable<boolean>;
      })
    );
  }
}
