import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { GenreService } from '../../../core/services/genre.service';
import { AuthService } from '../../../core/services/auth';
import { TenantService } from '../../../core/services/tenant.service';
import { GameGenreResponse } from '../../../core/services/theme';

/**
 * GenreListComponent shows all available game genres as themed preview cards.
 * Admins see a "New Genre" button to create custom genres.
 */
@Component({
  selector: 'app-genre-list',
  standalone: false,
  templateUrl: './genre-list.html',
  styleUrl: './genre-list.scss',
})
export class GenreList implements OnInit {
  readonly genres = signal<GameGenreResponse[]>([]);
  readonly loading = signal(false);

  constructor(
    private readonly genreService: GenreService,
    readonly authService: AuthService,
    private readonly tenantService: TenantService,
    private readonly router: Router,
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
}
