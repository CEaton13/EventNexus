import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PlayerDetail } from './player-detail/player-detail';
import { PlayerForm } from './player-form/player-form';

const routes: Routes = [
  { path: ':id', component: PlayerDetail },
  { path: ':id/edit', component: PlayerForm },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PlayersRoutingModule {}
