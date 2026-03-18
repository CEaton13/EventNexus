import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TeamsRoutingModule } from './teams-routing-module';
import { TeamList } from './team-list/team-list';
import { TeamDetail } from './team-detail/team-detail';

@NgModule({
  declarations: [TeamList, TeamDetail],
  imports: [CommonModule, TeamsRoutingModule],
})
export class TeamsModule {}
