import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PlayersRoutingModule } from './players-routing-module';
import { PlayerDetail } from './player-detail/player-detail';

@NgModule({
  declarations: [PlayerDetail],
  imports: [CommonModule, PlayersRoutingModule],
})
export class PlayersModule {}
