import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth-module').then(m => m.AuthModule)
  },
  {
    path: 'tournaments',
    loadChildren: () =>
      import('./features/tournaments/tournaments-module').then(m => m.TournamentsModule)
  },
  {
    path: 'teams',
    loadChildren: () =>
      import('./features/teams/teams-module').then(m => m.TeamsModule)
  },
  {
    path: 'players',
    loadChildren: () =>
      import('./features/players/players-module').then(m => m.PlayersModule)
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TOURNAMENT_ADMIN'] },
    loadChildren: () =>
      import('./features/admin/admin-module').then(m => m.AdminModule)
  },
  { path: 'unauthorized', redirectTo: '/tournaments' },
  { path: '', redirectTo: '/tournaments', pathMatch: 'full' },
  { path: '**', redirectTo: '/tournaments' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
