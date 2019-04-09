import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { FacilitiesOverviewComponent } from "./facilities-overview/facilities-overview.component";
import {FacilitiesDetailComponent} from "./facilities-detail/facilities-detail.component";
import {FacilityMoveToProductionComponent} from "./facilities-detail/facility-move-to-production/facility-move-to-production.component";
import {NotFoundPageComponent} from "../shared/not-found-page/not-found-page.component";
import {FacilityAddAdminComponent} from "./facilities-detail/facility-add-admin/facility-add-admin.component";
import {FacilityAddAdminSignComponent} from "./facilities-detail/facility-add-admin/facility-add-admin-sign/facility-add-admin-sign.component";

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
    path: 'moveToProduction/:id',
    component: FacilityMoveToProductionComponent
  },
  {
    path: 'addAdmin/sign',
    component: FacilityAddAdminSignComponent
  },
  {
    path: 'addAdmin/:id',
    component: FacilityAddAdminComponent
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
