import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VenueList } from './venue-list/venue-list';
import { VenueDetail } from './venue-detail/venue-detail';

const routes: Routes = [
  { path: '', component: VenueList },
  { path: ':id', component: VenueDetail },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VenuesRoutingModule {}
