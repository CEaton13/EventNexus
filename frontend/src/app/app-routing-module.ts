import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { orgGuard } from './core/guards/org.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth-module').then(m => m.AuthModule)
  },
  {
    // Org-scoped routes — all child routes inherit the active org context.
    path: ':orgSlug',
    canActivate: [authGuard, orgGuard],
    children: [
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
        canActivate: [roleGuard],
        data: { roles: ['TOURNAMENT_ADMIN'] },
        loadChildren: () =>
          import('./features/admin/admin-module').then(m => m.AdminModule)
      },
      { path: '', redirectTo: 'tournaments', pathMatch: 'full' },
    ]
  },
  { path: 'unauthorized', redirectTo: '/auth/login' },
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
