import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GenreService } from '../../../core/services/genre.service';
import { TenantService } from '../../../core/services/tenant.service';
import { GameGenreResponse } from '../../../core/services/theme';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';

/**
 * AdminGenreListComponent shows all game genres with admin CRUD controls.
 */
@Component({
  selector: 'app-admin-genre-list',
  standalone: false,
  templateUrl: './admin-genre-list.html',
  styleUrl: './admin-genre-list.scss',
})
export class AdminGenreList implements OnInit {
  readonly genres = signal<GameGenreResponse[]>([]);
  readonly loading = signal(false);

  constructor(
    private readonly genreService: GenreService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.genreService.getAll().subscribe({
      next: genres => {
        this.genres.set(genres);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  createGenre(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'genres', 'new']);
  }

  editGenre(id: number): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'genres', id, 'edit']);
  }

  deleteGenre(genre: GameGenreResponse): void {
    this.dialog.open(ConfirmDialog, {
      panelClass: 'dark-dialog',
      data: {
        title: 'Delete Genre',
        message: `Delete the "${genre.name}" genre? Tournaments using this genre will lose their theme.`,
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
      },
    }).afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.genreService.delete(genre.id).subscribe({
        next: () => {
          this.snackBar.open('Genre deleted', 'OK', { duration: 3000 });
          this.load();
        },
        error: () => this.snackBar.open('Cannot delete — genre is in use', 'OK', { duration: 4000 }),
      });
    });
  }
}
