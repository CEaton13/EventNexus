import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Dashboard } from './dashboard/dashboard';
import { TournamentNew } from './tournament-new/tournament-new';

const routes: Routes = [
  { path: 'dashboard', component: Dashboard },
  { path: 'tournaments/new', component: TournamentNew },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
