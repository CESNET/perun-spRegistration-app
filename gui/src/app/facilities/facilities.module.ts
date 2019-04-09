import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FacilitiesRoutingModule } from './facilities-routing.module';
import { FacilitiesOverviewComponent } from "./facilities-overview/facilities-overview.component";
import { SharedModule } from "../shared/shared.module";
import { FacilitiesDetailComponent } from './facilities-detail/facilities-detail.component';
import {FacilityAttributeValuePipe} from "./facility-attribute-value.pipe";
import { FacilityMoveToProductionComponent } from './facilities-detail/facility-move-to-production/facility-move-to-production.component';
import { FacilitiesDetailDialogComponent } from './facilities-detail/facilities-detail-dialog/facilities-detail-dialog.component';

@NgModule({
  imports: [
    CommonModule,
    FacilitiesRoutingModule,
    SharedModule
  ],
  declarations: [
    FacilitiesOverviewComponent,
    FacilitiesDetailComponent,
    FacilityAttributeValuePipe,
    FacilityMoveToProductionComponent,
    FacilitiesDetailDialogComponent
  ],
  entryComponents: [
    FacilitiesDetailDialogComponent
  ]
})
export class FacilitiesModule { }
