import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

/**
 * ConfirmDialogComponent is a reusable confirmation dialog for destructive actions
 * (delete team, delete player, archive tournament, etc.).
 *
 * Usage:
 * ```ts
 * this.dialog.open(ConfirmDialog, { data: { title: 'Delete Team', message: 'Are you sure?' } })
 *   .afterClosed().subscribe(confirmed => { if (confirmed) { ... } });
 * ```
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: false,
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  constructor(
    public readonly dialogRef: MatDialogRef<ConfirmDialog>,
    @Inject(MAT_DIALOG_DATA) public readonly data: ConfirmDialogData,
  ) {}
}
