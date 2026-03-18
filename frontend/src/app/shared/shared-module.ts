import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenrePreview } from './components/genre-preview/genre-preview';

@NgModule({
  declarations: [GenrePreview],
  imports: [CommonModule],
  exports: [GenrePreview],
})
export class SharedModule {}
