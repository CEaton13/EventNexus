import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TournamentHubComponent } from './tournament-hub';

const routes: Routes = [
  { path: ':id', component: TournamentHubComponent },
  { path: '', redirectTo: '/tournaments', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TournamentHubRoutingModule {}
