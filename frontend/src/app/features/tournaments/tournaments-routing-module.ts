import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TournamentList } from './tournament-list/tournament-list';
import { TournamentDetail } from './tournament-detail/tournament-detail';

const routes: Routes = [
  { path: '', component: TournamentList },
  { path: ':id', component: TournamentDetail }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TournamentsRoutingModule {}
