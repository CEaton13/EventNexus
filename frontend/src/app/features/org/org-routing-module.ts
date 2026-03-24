import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreateOrg } from './create-org/create-org';

const routes: Routes = [
  { path: 'create', component: CreateOrg },
  { path: '', redirectTo: 'create', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class OrgRoutingModule {}
