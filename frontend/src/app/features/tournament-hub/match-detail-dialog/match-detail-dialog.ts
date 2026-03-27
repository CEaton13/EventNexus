import { Component, DestroyRef, Inject, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatchService } from '../../../core/services/match.service';
import { MatchDetail, MatchStatus } from '../../../shared/models/match.model';

/**
 * MatchDetailDialogComponent displays read-only details for a single match.
 *
 * <p>Opened with {@code { data: { matchId: number } }}. Fetches match details
 * from the public {@code GET /api/matches/:id} endpoint on init.</p>
 */
@Component({
  selector: 'app-match-detail-dialog',
  standalone: false,
  templateUrl: './match-detail-dialog.html',
  styleUrl: './match-detail-dialog.scss',
})
export class MatchDetailDialog implements OnInit {
  readonly match = signal<MatchDetail | null>(null);
  readonly loading = signal(false);

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    @Inject(MAT_DIALOG_DATA) readonly data: { matchId: number },
    private readonly dialogRef: MatDialogRef<MatchDetailDialog>,
    private readonly matchService: MatchService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.matchService
      .getById(this.data.matchId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (m) => {
          this.match.set(m);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.snackBar.open('Failed to load match details.', 'Dismiss', { duration: 4000 });
        },
      });
  }

  /**
   * Maps a {@link MatchStatus} enum value to a human-readable label.
   * @param status The match status to convert.
   */
  statusLabel(status: MatchStatus): string {
    const labels: Record<MatchStatus, string> = {
      UNSCHEDULED: 'Not Scheduled',
      SCHEDULED: 'Scheduled',
      IN_PROGRESS: 'In Progress',
      COMPLETED: 'Completed',
      BYE: 'Bye',
    };
    return labels[status] ?? status;
  }

  /** Closes the dialog. */
  close(): void {
    this.dialogRef.close();
  }
}
