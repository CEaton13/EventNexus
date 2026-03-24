import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TournamentService } from '../../../core/services/tournament.service';
import { GenreService } from '../../../core/services/genre.service';
import { VenueService } from '../../../core/services/venue.service';
import { TenantService } from '../../../core/services/tenant.service';
import { GameGenreResponse } from '../../../core/services/theme';
import { VenueResponse } from '../../../shared/models/venue.model';

/**
 * TournamentWizardComponent guides an admin through a 5-step process to
 * create a new tournament: basic info → genre → venue → review → confirm.
 */
@Component({
  selector: 'app-tournament-new',
  standalone: false,
  templateUrl: './tournament-new.html',
  styleUrl: './tournament-new.scss',
})
export class TournamentNew implements OnInit {
  readonly genres = signal<GameGenreResponse[]>([]);
  readonly venues = signal<VenueResponse[]>([]);
  readonly loading = signal(false);
  readonly selectedGenre = signal<GameGenreResponse | null>(null);

  readonly basicForm: FormGroup;
  readonly genreForm: FormGroup;
  readonly venueForm: FormGroup;

  readonly formatOptions = [
    { value: 'SINGLE_ELIMINATION', label: 'Single Elimination' },
    { value: 'DOUBLE_ELIMINATION', label: 'Double Elimination' },
    { value: 'ROUND_ROBIN', label: 'Round Robin' },
  ];

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly tournamentService: TournamentService,
    private readonly genreService: GenreService,
    private readonly venueService: VenueService,
    private readonly tenantService: TenantService,
  ) {
    this.basicForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', Validators.maxLength(2000)],
      gameTitle: ['', Validators.maxLength(200)],
      format: ['SINGLE_ELIMINATION', Validators.required],
      maxTeams: [16, [Validators.required, Validators.min(2), Validators.max(512)]],
      registrationStart: ['', Validators.required],
      registrationEnd: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
    });

    this.genreForm = this.fb.group({
      gameGenreId: [null, Validators.required],
    });

    this.venueForm = this.fb.group({
      venueId: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.genreService.getAll().subscribe(g => this.genres.set(g));
    this.venueService.getAll().subscribe(v => this.venues.set(v));
  }

  selectGenre(genre: GameGenreResponse): void {
    this.selectedGenre.set(genre);
    this.genreForm.patchValue({ gameGenreId: genre.id });
  }

  submit(): void {
    if (this.basicForm.invalid || this.genreForm.invalid || this.venueForm.invalid) return;
    this.loading.set(true);

    const payload = {
      ...this.basicForm.value,
      ...this.genreForm.value,
      ...this.venueForm.value,
    };

    this.tournamentService.create(payload).subscribe({
      next: t => {
        this.snackBar.open('Tournament created!', 'OK', { duration: 4000 });
        this.router.navigate([this.tenantService.currentOrgSlug(), 'tournaments', t.id]);
      },
      error: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate([this.tenantService.currentOrgSlug(), 'admin', 'dashboard']);
  }

  venueById(id: number): VenueResponse | undefined {
    return this.venues().find(v => v.id === id);
  }
}
