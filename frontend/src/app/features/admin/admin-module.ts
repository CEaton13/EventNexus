import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing-module';
import { Dashboard } from './dashboard/dashboard';
import { TournamentNew } from './tournament-new/tournament-new';

@NgModule({
  declarations: [Dashboard, TournamentNew],
  imports: [CommonModule, AdminRoutingModule],
})
export class AdminModule {}
