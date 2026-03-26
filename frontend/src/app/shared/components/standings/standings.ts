import {
  Component,
  Input,
  ViewChild,
  AfterViewInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { StandingsResponse } from '../../models/tournament.model';

/**
 * StandingsComponent renders a ranked table of teams for a given tournament.
 * Supports sorting via MatSort and optional rank-delta indicators when a
 * previous-standings snapshot is supplied via the `previousStandings` input.
 * Used inside TournamentDetailComponent.
 */
@Component({
  selector: 'app-standings',
  standalone: false,
  templateUrl: './standings.html',
  styleUrl: './standings.scss',
})
export class Standings implements AfterViewInit, OnChanges {
  /** Ordered standings rows from the /standings endpoint. */
  @Input() standings: StandingsResponse[] = [];

  /**
   * Optional prior standings snapshot. When provided, rank-delta indicators
   * are shown next to each team's current rank.
   */
  @Input() previousStandings: StandingsResponse[] | null = null;

  @ViewChild(MatSort) sort!: MatSort;

  readonly dataSource = new MatTableDataSource<StandingsResponse>([]);

  readonly displayedColumns = ['rank', 'delta', 'team', 'wins', 'losses', 'points'];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['standings']) {
      this.dataSource.data = this.standings;
    }
    if (changes['standings'] || changes['previousStandings']) {
      this.dataSource.sortingDataAccessor = (row, columnId) => {
        if (columnId === 'delta') {
          return this.getRankDelta(row);
        }
        switch (columnId) {
          case 'rank':
            return row.rank;
          case 'team':
            return row.teamName;
          case 'wins':
            return row.wins;
          case 'losses':
            return row.losses;
          case 'points':
            return row.points;
          default:
            return '';
        }
      };
    }
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  /**
   * Returns the rank improvement for a team relative to the previous standings
   * snapshot. Positive values mean the team moved up; negative means down.
   * Returns 0 when no previous snapshot is available or the team is new.
   */
  getRankDelta(row: StandingsResponse): number {
    if (!this.previousStandings) {
      return 0;
    }
    const previous = this.previousStandings.find((s) => s.teamId === row.teamId);
    if (!previous) {
      return 0;
    }
    return previous.rank - row.rank;
  }

  /**
   * Returns a CSS class name based on the rank delta direction for a team.
   */
  getDeltaClass(row: StandingsResponse): string {
    const delta = this.getRankDelta(row);
    if (delta > 0) return 'rank-up';
    if (delta < 0) return 'rank-down';
    return 'rank-same';
  }
}
