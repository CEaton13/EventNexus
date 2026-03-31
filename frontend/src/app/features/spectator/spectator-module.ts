import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { SpectatorRoutingModule } from './spectator-routing-module';
import { MyRegistrations } from './my-registrations/my-registrations';
import { MyTournaments } from './my-tournaments/my-tournaments';
import { Following } from './following/following';

@NgModule({
  declarations: [MyRegistrations, MyTournaments, Following],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule,
    SpectatorRoutingModule,
  ],
})
export class SpectatorModule {}
