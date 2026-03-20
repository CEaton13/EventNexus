import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TournamentsRoutingModule } from './tournaments-routing-module';
import { TournamentList } from './tournament-list/tournament-list';
import { TournamentDetail } from './tournament-detail/tournament-detail';

@NgModule({
  declarations: [TournamentList, TournamentDetail],
  imports: [CommonModule, TournamentsRoutingModule],
})
export class TournamentsModule {}
