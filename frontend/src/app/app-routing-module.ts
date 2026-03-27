import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { orgGuard } from './core/guards/org.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth-module').then((m) => m.AuthModule),
  },
  // Global public tournament list — no auth, no org required (spectator entry point).
  // Must be declared before :orgSlug to prevent Angular matching 'tournaments' as a slug.
  {
    path: 'tournaments',
    loadChildren: () =>
      import('./features/tournaments/tournaments-module').then((m) => m.TournamentsModule),
  },
  // Global public genre list — no auth, no org required.
  // Must be declared before :orgSlug to prevent Angular matching 'genres' as a slug.
  {
    path: 'genres',
    loadChildren: () => import('./features/genres/genres-module').then((m) => m.GenresModule),
  },
  // Public tournament hub — /t/:id — no auth, no org required.
  // Literal path 't' must appear before :orgSlug to prevent Angular treating 't' as a slug.
  {
    path: 't',
    loadChildren: () =>
      import('./features/tournament-hub/tournament-hub-module').then((m) => m.TournamentHubModule),
  },
  // Public team roster — /teams/:id — no auth, no org required.
  // Must appear before :orgSlug to prevent Angular treating 'teams' as a slug.
  {
    path: 'teams',
    loadChildren: () => import('./features/teams/teams-module').then((m) => m.TeamsModule),
  },
  // Org creation — TOURNAMENT_ADMIN only, accessed from landing CTA when user has no org.
  {
    path: 'org',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['TOURNAMENT_ADMIN'] },
    loadChildren: () => import('./features/org/org-module').then((m) => m.OrgModule),
  },
  {
    // Org-scoped routes — all child routes inherit the active org context.
    path: ':orgSlug',
    canActivate: [authGuard, orgGuard],
    children: [
      {
        path: 'tournaments',
        loadChildren: () =>
          import('./features/tournaments/tournaments-module').then((m) => m.TournamentsModule),
      },
      {
        path: 'teams',
        loadChildren: () => import('./features/teams/teams-module').then((m) => m.TeamsModule),
      },
      {
        path: 'players',
        loadChildren: () =>
          import('./features/players/players-module').then((m) => m.PlayersModule),
      },
      {
        path: 'venues',
        loadChildren: () => import('./features/venues/venues-module').then((m) => m.VenuesModule),
      },
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['TOURNAMENT_ADMIN'] },
        loadChildren: () => import('./features/admin/admin-module').then((m) => m.AdminModule),
      },
      { path: '', redirectTo: 'tournaments', pathMatch: 'full' },
    ],
  },
  { path: '', redirectTo: 'tournaments', pathMatch: 'full' },
  { path: 'unauthorized', redirectTo: '/tournaments' },
  { path: '**', redirectTo: '/tournaments' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
