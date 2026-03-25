import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Dashboard } from './dashboard/dashboard';
import { TournamentNew } from './tournament-new/tournament-new';
import { AdminGenreList } from './genre-list/admin-genre-list';
import { AdminGenreForm } from './genre-form/admin-genre-form';
import { VenueForm } from '../venues/venue-form/venue-form';

const routes: Routes = [
  { path: 'dashboard', component: Dashboard },
  { path: 'tournaments/new', component: TournamentNew },
  { path: 'genres', component: AdminGenreList },
  { path: 'genres/new', component: AdminGenreForm },
  { path: 'genres/:id/edit', component: AdminGenreForm },
  { path: 'venues/new', component: VenueForm },
  { path: 'venues/:id/edit', component: VenueForm },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
