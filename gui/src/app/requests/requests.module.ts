import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RequestsRoutingModule } from './requests-routing.module';
import { RequestsOverviewComponent } from './requests-overview/requests-overview.component';
import { SharedModule } from '../shared/shared.module';
import { NewRequestComponent } from './new-request/new-request.component';
import { RequestDetailComponent } from './request-detail/request-detail.component';
import { RequestItemValuePipe } from './request-item-value.pipe';
import { AllRequestsComponent } from './all-requests/all-requests.component';
import { RequestCreationStepComponent } from './new-request/request-creation-step/request-creation-step.component';
import { RequestDetailDialogComponent } from './request-detail/request-detail-dialog/request-detail-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { RequestEditComponent } from './request-edit/request-edit.component';
import { RequestActionPipe } from './request-action.pipe';
import { RequestApprovedPipe } from './request-approved.pipe';
import { MatPaginatorModule } from '@angular/material/paginator';
import {MatTabsModule} from "@angular/material/tabs";
import { RequestDetailItemLocalePipe } from './request-detail-item-locale.pipe';
import {RequestStatusIconPipe} from "./request-status-icon.pipe";
import {RequestStatusLangPipe} from "./request-status-lang.pipe";
import {RequestOverview} from "../core/models/RequestOverview";
import {MatTableDataSource} from "@angular/material/table";
import {TranslateService} from "@ngx-translate/core";

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
    RequestsOverviewComponent,
    NewRequestComponent,
    RequestDetailComponent,
    RequestItemValuePipe,
    RequestCreationStepComponent,
    AllRequestsComponent,
    RequestEditComponent,
    RequestStatusIconPipe,
    RequestStatusLangPipe,
    RequestActionPipe,
    AllRequestsComponent,
    RequestDetailDialogComponent,
    RequestApprovedPipe,
    RequestDetailItemLocalePipe
  ],
})
export class RequestsModule { }
