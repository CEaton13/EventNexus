import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Dashboard } from './dashboard/dashboard';
import { TournamentNew } from './tournament-new/tournament-new';
import { AdminGenreList } from './genre-list/admin-genre-list';
import { AdminGenreForm } from './genre-form/admin-genre-form';
import { VenueForm } from '../venues/venue-form/venue-form';
import { AdminLayout } from './admin-layout/admin-layout';
import { RegistrationManager } from './registration-manager/registration-manager';
import { EquipmentList } from './equipment-list/equipment-list';
import { EquipmentForm } from './equipment-form/equipment-form';
import { EquipmentLoadout } from './equipment-loadout/equipment-loadout';
import { MatchScheduler } from './match-scheduler/match-scheduler';
import { RegistrationsHub } from './registrations-hub/registrations-hub';
import { SchedulerHub } from './scheduler-hub/scheduler-hub';
import { AdminVenueList } from './venue-list/admin-venue-list';

const routes: Routes = [
  {
    path: '',
    component: AdminLayout,
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'tournaments/new', component: TournamentNew },
      { path: 'tournaments/:id/registrations', component: RegistrationManager },
      { path: 'registrations', component: RegistrationsHub },
      { path: 'genres', component: AdminGenreList },
      { path: 'genres/new', component: AdminGenreForm },
      { path: 'genres/:id/edit', component: AdminGenreForm },
      { path: 'venues', component: AdminVenueList },
      { path: 'venues/new', component: VenueForm },
      { path: 'venues/:id/edit', component: VenueForm },
      { path: 'equipment', component: EquipmentList },
      { path: 'equipment/new', component: EquipmentForm },
      { path: 'equipment/:id/edit', component: EquipmentForm },
      { path: 'equipment/loadouts/:tournamentId', component: EquipmentLoadout },
      { path: 'tournaments/:id/schedule', component: MatchScheduler },
      { path: 'matches/schedule', component: SchedulerHub },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
