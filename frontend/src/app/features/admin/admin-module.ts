import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { LayoutModule } from '@angular/cdk/layout';

import { AdminRoutingModule } from './admin-routing-module';
import { Dashboard } from './dashboard/dashboard';
import { TournamentNew } from './tournament-new/tournament-new';
import { AdminGenreList } from './genre-list/admin-genre-list';
import { AdminGenreForm } from './genre-form/admin-genre-form';
import { VenueForm } from '../venues/venue-form/venue-form';
import { AdminLayout } from './admin-layout/admin-layout';
import { RegistrationManager } from './registration-manager/registration-manager';
import { EquipmentList } from './equipment-list/equipment-list';
import { EquipmentForm } from './equipment-form/equipment-form';
import { EquipmentLoadout } from './equipment-loadout/equipment-loadout';
import { MatchScheduler } from './match-scheduler/match-scheduler';
import { SharedModule } from '../../shared/shared-module';

@NgModule({
  declarations: [
    Dashboard,
    TournamentNew,
    AdminGenreList,
    AdminGenreForm,
    VenueForm,
    AdminLayout,
    RegistrationManager,
    EquipmentList,
    EquipmentForm,
    EquipmentLoadout,
    MatchScheduler,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatStepperModule,
    MatSnackBarModule,
    MatSidenavModule,
    MatToolbarModule,
    MatCheckboxModule,
    MatTabsModule,
    MatProgressBarModule,
    MatBadgeModule,
    MatTableModule,
    MatSortModule,
    MatDialogModule,
    MatChipsModule,
    LayoutModule,
    AdminRoutingModule,
  ],
})
export class AdminModule {}
