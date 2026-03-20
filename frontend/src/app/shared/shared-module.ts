import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { GenrePreview } from './components/genre-preview/genre-preview';
import { OrgSelector } from './components/org-selector/org-selector';

@NgModule({
  declarations: [GenrePreview, OrgSelector],
  imports: [CommonModule, MatFormFieldModule, MatSelectModule],
  exports: [GenrePreview, OrgSelector],
})
export class SharedModule {}
