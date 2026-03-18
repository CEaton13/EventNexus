import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PlayerDetail } from './player-detail/player-detail';

const routes: Routes = [
  { path: ':id', component: PlayerDetail }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PlayersRoutingModule {}
