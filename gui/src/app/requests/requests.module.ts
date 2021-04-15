import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RequestsRoutingModule } from './requests-routing.module';
import { RequestsUserComponent } from './requests-user/requests-user.component';
import { SharedModule } from '../shared/shared.module';
import { RequestsRegisterServiceComponent } from './requests-register-service/requests-register-service.component';
import { RequestsDetailComponent } from './requests-detail/requests-detail.component';
import { RequestsItemValuePipe } from './requests-item-value.pipe';
import { RequestsAdminComponent } from './requests-admin/requests-admin.component';
import { RequestsRegisterServiceStepComponent } from './requests-register-service/requests-register-service-step/requests-register-service-step.component';
import { RequestsDetailDialogComponent } from './requests-detail/requests-detail-dialog/requests-detail-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { RequestsEditComponent } from './requests-edit/requests-edit.component';
import { RequestsActionPipe } from './requests-action.pipe';
import { MatPaginatorModule } from '@angular/material/paginator';
import {MatTabsModule} from "@angular/material/tabs";
import {RequestsStatusIconPipe} from "./requests-status-icon.pipe";
import {RequestsStatusLangPipe} from "./requests-status-lang.pipe";
import {RequestSignatureDecisionIconPipePipe} from "./requests-signature-decision-icon.pipe";

@NgModule({
  imports: [
    CommonModule,
    RequestsRoutingModule,
    SharedModule,
    MatDialogModule,
    MatPaginatorModule,
    MatTabsModule,
  ],
  declarations: [
    RequestsUserComponent,
    RequestsRegisterServiceComponent,
    RequestsDetailComponent,
    RequestsItemValuePipe,
    RequestsRegisterServiceStepComponent,
    RequestsAdminComponent,
    RequestsEditComponent,
    RequestsStatusIconPipe,
    RequestsStatusLangPipe,
    RequestsActionPipe,
    RequestsAdminComponent,
    RequestsDetailDialogComponent,
    RequestSignatureDecisionIconPipePipe
  ],
})
export class RequestsModule { }
