import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { Landing } from './landing/landing';

const routes: Routes = [
  { path: '', component: Landing, pathMatch: 'full' },
];

@NgModule({
  declarations: [Landing],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatButtonModule,
  ],
})
export class HomeModule {}
