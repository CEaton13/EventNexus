import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSortModule } from '@angular/material/sort';
import { MatTabsModule } from '@angular/material/tabs';

import { GenrePreview } from './components/genre-preview/genre-preview';
import { GenreThemePreview } from './components/genre-theme-preview/genre-theme-preview';
import { OrgSelector } from './components/org-selector/org-selector';
import { Standings } from './components/standings/standings';
import { BracketViewer } from './components/bracket-viewer/bracket-viewer';
import { ConfirmDialog } from './components/confirm-dialog/confirm-dialog';
import { AuthDialog } from './components/auth-dialog/auth-dialog';
import { ReplacePipe } from './pipes/replace.pipe';
import { MatchDetailDialog } from '../features/tournament-hub/match-detail-dialog/match-detail-dialog';

@NgModule({
  declarations: [
    GenrePreview,
    GenreThemePreview,
    OrgSelector,
    Standings,
    BracketViewer,
    ConfirmDialog,
    AuthDialog,
    ReplacePipe,
    MatchDetailDialog,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatSortModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatButtonModule,
    MatTabsModule,
  ],
  exports: [
    GenrePreview,
    GenreThemePreview,
    OrgSelector,
    Standings,
    BracketViewer,
    ConfirmDialog,
    AuthDialog,
    ReplacePipe,
    MatchDetailDialog,
  ],
})
export class SharedModule {}
