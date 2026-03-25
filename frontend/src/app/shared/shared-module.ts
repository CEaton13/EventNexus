import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

import { GenrePreview } from './components/genre-preview/genre-preview';
import { OrgSelector } from './components/org-selector/org-selector';
import { Standings } from './components/standings/standings';
import { BracketViewer } from './components/bracket-viewer/bracket-viewer';
import { ConfirmDialog } from './components/confirm-dialog/confirm-dialog';

@NgModule({
  declarations: [GenrePreview, OrgSelector, Standings, BracketViewer, ConfirmDialog],
  imports: [
    CommonModule,
    RouterModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatButtonModule,
  ],
  exports: [GenrePreview, OrgSelector, Standings, BracketViewer, ConfirmDialog],
})
export class SharedModule {}
