import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RequestsRoutingModule } from './requests-routing.module';
import { RequestsOverviewComponent } from "./requests-overview/requests-overview.component";
import { SharedModule } from "../shared/shared.module";
import { NewRequestComponent } from './new-request/new-request.component';
import { ApplicationItemComponent } from './new-request/application-item/application-item.component';
import { ApplicationItemStringComponent } from './new-request/application-item/application-item-string/application-item-string.component';
import { ApplicationItemBooleanComponent } from './new-request/application-item/application-item-boolean/application-item-boolean.component';
import { ApplicationItemListComponent } from './new-request/application-item/application-item-list/application-item-list.component';
import { ApplicationItemMapComponent } from './new-request/application-item/application-item-map/application-item-map.component';
import { ApplicationItemSelectComponent } from './new-request/application-item/application-item-select/application-item-select.component';
import { RequestDetailComponent } from './request-detail/request-detail.component';
import { RequestItemValuePipe } from './request-item-value.pipe';
import { AllRequestsComponent } from './all-requests/all-requests.component';
import {RequestCreationStepComponent} from "./new-request/request-creation-step/request-creation-step.component";
import { RequestDetailDialogComponent } from './request-detail/request-detail-dialog/request-detail-dialog.component';
import {MatDialogModule} from "@angular/material";
import { RequestEditComponent } from './request-edit/request-edit.component';
import { RequestStatusPipe } from './request-status.pipe';
import { RequestActionPipe } from './request-action.pipe';

@NgModule({
  imports: [
    CommonModule,
    RequestsRoutingModule,
    SharedModule,
    MatDialogModule
  ],
  declarations: [
    RequestsOverviewComponent,
    NewRequestComponent,
    RequestDetailComponent,
    RequestItemValuePipe,
    RequestCreationStepComponent,
    AllRequestsComponent,
    RequestEditComponent,
    RequestStatusPipe,
    RequestActionPipe,
    AllRequestsComponent,
    RequestDetailDialogComponent,
  ],
  entryComponents: [
    RequestDetailDialogComponent
  ]
})
export class RequestsModule { }
