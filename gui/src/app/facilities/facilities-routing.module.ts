import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { FacilitiesOverviewComponent } from "./facilities-overview/facilities-overview.component";
import {FacilitiesDetailComponent} from "./facilities-detail/facilities-detail.component";
import {NotFoundPageComponent} from "../shared/not-found-page/not-found-page.component";

const routes: Routes = [
  {
    path: 'myFacilities',
    component: FacilitiesOverviewComponent
  },
  {
      path: 'detail/:id',
      component: FacilitiesDetailComponent
  },
  {
    path: '**',
    component: NotFoundPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FacilitiesRoutingModule { }
