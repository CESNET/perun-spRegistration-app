import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FacilitiesUserComponent } from './facilities-user/facilities-user.component';
import { FacilitiesDetailComponent } from './facilities-detail/facilities-detail.component';
import { NotFoundPageComponent } from '../shared/not-found-page/not-found-page.component';
import { FacilityAddAdminComponent } from './facilities-detail/facility-add-admin/facility-add-admin.component';
import { FacilityAddAdminSignComponent } from './facilities-detail/facility-add-admin/facility-add-admin-sign/facility-add-admin-sign.component';
import { FacilitiesEditComponent } from './facilities-edit/facilities-edit.component';
import { FacilitiesAdminComponent } from './facilities-admin/facilities-admin.component';
import { FacilitiesTransferComponent } from './facilities-transfer/facilities-transfer.component';

const routes: Routes = [
  {
    path: 'myServices',
    component: FacilitiesUserComponent,
  },
  {
    path: 'allFacilities',
    component: FacilitiesAdminComponent,
  },
  {
    path: 'detail/:id',
    component: FacilitiesDetailComponent,
  },
  {
    path: 'moveToProduction/:id',
    component: FacilitiesTransferComponent,
  },
  {
    path: 'edit/:id',
    component: FacilitiesEditComponent,
  },
  {
    path: 'addAdmin/sign',
    component: FacilityAddAdminSignComponent,
  },
  {
    path: 'addAdmin/:id',
    component: FacilityAddAdminComponent,
  },
  {
    path: '**',
    component: NotFoundPageComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FacilitiesRoutingModule {}
