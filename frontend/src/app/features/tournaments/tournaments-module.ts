import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { TournamentsRoutingModule } from './tournaments-routing-module';
import { TournamentList } from './tournament-list/tournament-list';
import { TournamentDetail } from './tournament-detail/tournament-detail';
import { SharedModule } from '../../shared/shared-module';

@NgModule({
  declarations: [TournamentList, TournamentDetail],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    SharedModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatListModule,
    MatDividerModule,
    MatSnackBarModule,
    TournamentsRoutingModule,
  ],
})
export class TournamentsModule {}
