import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TeamList } from './team-list/team-list';
import { TeamDetail } from './team-detail/team-detail';

const routes: Routes = [
  { path: '', component: TeamList },
  { path: ':id', component: TeamDetail }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TeamsRoutingModule {}
