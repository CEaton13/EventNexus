import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TeamList } from './team-list/team-list';
import { TeamDetail } from './team-detail/team-detail';
import { TeamForm } from './team-form/team-form';
import { TeamPortal } from './team-portal/team-portal';

const routes: Routes = [
  { path: '', component: TeamList },
  { path: 'new', component: TeamForm },
  { path: ':id/edit', component: TeamForm },
  { path: ':id/portal', component: TeamPortal },
  { path: ':id', component: TeamDetail },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TeamsRoutingModule {}
