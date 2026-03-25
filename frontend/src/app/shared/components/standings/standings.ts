import { Component, Input } from '@angular/core';
import { StandingsResponse } from '../../models/tournament.model';

/**
 * StandingsComponent renders a ranked table of teams for a given tournament.
 * Used inside TournamentDetailComponent.
 */
@Component({
  selector: 'app-standings',
  standalone: false,
  templateUrl: './standings.html',
  styleUrl: './standings.scss',
})
export class Standings {
  /** Ordered standings rows from the /standings endpoint. */
  @Input() standings: StandingsResponse[] = [];

  readonly displayedColumns = ['rank', 'team', 'wins', 'losses', 'points'];
}
