import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { MyRegistrations } from './my-registrations/my-registrations';
import { MyTournaments } from './my-tournaments/my-tournaments';
import { Following } from './following/following';

const routes: Routes = [
  { path: 'registrations', component: MyRegistrations },
  { path: 'tournaments', component: MyTournaments },
  { path: 'following', component: Following },
  { path: '', redirectTo: 'registrations', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SpectatorRoutingModule {}
