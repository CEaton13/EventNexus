import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { GenresRoutingModule } from './genres-routing-module';
import { GenreList } from './genre-list/genre-list';
import { SharedModule } from '../../shared/shared-module';

@NgModule({
  declarations: [GenreList],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    GenresRoutingModule,
  ],
})
export class GenresModule {}
