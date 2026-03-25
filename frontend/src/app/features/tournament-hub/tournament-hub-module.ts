import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { SharedModule } from '../../shared/shared-module';
import { TournamentHubComponent } from './tournament-hub';
import { TournamentHubRoutingModule } from './tournament-hub-routing-module';

@NgModule({
  declarations: [TournamentHubComponent],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    TournamentHubRoutingModule,
  ],
})
export class TournamentHubModule {}
