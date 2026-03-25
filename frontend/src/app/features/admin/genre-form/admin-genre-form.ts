import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GenreService } from '../../../core/services/genre.service';
import { TenantService } from '../../../core/services/tenant.service';
import { ThemeService } from '../../../core/services/theme';

/** All background style options matching the BackgroundStyle enum. */
const BACKGROUND_STYLES = ['DARK', 'NEON', 'GRITTY', 'SLEEK', 'PIXEL', 'ARCANE'] as const;

/**
 * AdminGenreFormComponent handles creating and editing game genres.
 * Shows a live theme preview that updates as the admin adjusts colors.
 */
@Component({
  selector: 'app-admin-genre-form',
  standalone: false,
  templateUrl: './admin-genre-form.html',
  styleUrl: './admin-genre-form.scss',
})
export class AdminGenreForm implements OnInit {
  form!: FormGroup;
  readonly backgroundStyles = BACKGROUND_STYLES;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly isEdit = signal(false);

  private genreId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly genreService: GenreService,
    private readonly tenantService: TenantService,
    private readonly themeService: ThemeService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      primaryColor: ['#7c3aed', [Validators.required, Validators.pattern(/^#[0-9A-Fa-f]{6}$/)]],
      secondaryColor: ['#4f46e5', [Validators.required, Validators.pattern(/^#[0-9A-Fa-f]{6}$/)]],
      accentColor: ['#06b6d4', [Validators.required, Validators.pattern(/^#[0-9A-Fa-f]{6}$/)]],
      backgroundStyle: ['DARK', Validators.required],
      fontFamily: ['Rajdhani, sans-serif', [Validators.required, Validators.maxLength(100)]],
      iconPackKey: ['default', [Validators.required, Validators.maxLength(50)]],
      heroImageUrl: ['', Validators.maxLength(500)],
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.genreId = Number(id);
      this.isEdit.set(true);
      this.loadGenre();
    }
  }

  private loadGenre(): void {
    this.loading.set(true);
    this.genreService.getById(this.genreId!).subscribe({
      next: genre => {
        this.form.patchValue({
          name: genre.name,
          primaryColor: genre.primaryColor,
          secondaryColor: genre.secondaryColor,
          accentColor: genre.accentColor,
          backgroundStyle: genre.backgroundStyle,
          fontFamily: genre.fontFamily,
          iconPackKey: genre.iconPackKey,
          heroImageUrl: genre.heroImageUrl ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Genre not found', 'OK', { duration: 3000 });
        this.cancel();
      },
    });
  }

  /** Builds a preview genre object from the current form values for the GenrePreview. */
  get previewGenre() {
    const v = this.form.value;
    return {
      id: 0,
      name: v.name || 'Preview',
      primaryColor: v.primaryColor,
      secondaryColor: v.secondaryColor,
      accentColor: v.accentColor,
      backgroundStyle: v.backgroundStyle,
      fontFamily: v.fontFamily,
      iconPackKey: v.iconPackKey,
    };
  }

  applyPreviewTheme(): void {
    this.themeService.applyGenre(this.previewGenre);
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const payload = { ...this.form.value };
    if (!payload.heroImageUrl) delete payload.heroImageUrl;

    const request$ = this.isEdit()
      ? this.genreService.update(this.genreId!, payload)
      : this.genreService.create(payload);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEdit() ? 'Genre updated' : 'Genre created',
          'OK',
          { duration: 3000 },
        );
        this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'genres']);
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Failed to save genre', 'OK', { duration: 3000 });
      },
    });
  }

  cancel(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'genres']);
  }
}
