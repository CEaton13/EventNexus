import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GenreList } from './genre-list/genre-list';

const routes: Routes = [
  { path: '', component: GenreList },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class GenresRoutingModule {}
