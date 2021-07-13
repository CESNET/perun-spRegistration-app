import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FacilitiesRoutingModule } from './facilities-routing.module';
import { FacilitiesUserComponent } from './facilities-user/facilities-user.component';
import { SharedModule } from '../shared/shared.module';
import { FacilitiesDetailComponent } from './facilities-detail/facilities-detail.component';
import {FacilitiesAttributeValuePipe} from './facilities-attribute-value.pipe';
import { FacilityMoveToProductionComponent } from './facilities-detail/facility-move-to-production/facility-move-to-production.component';
import { FacilitiesDetailDeleteDialogComponent } from './facilities-detail/facilities-detail-delete-dialog/facilities-detail-delete-dialog.component';
import { FacilityAddAdminComponent } from './facilities-detail/facility-add-admin/facility-add-admin.component';
import { FacilityAddAdminSignComponent } from './facilities-detail/facility-add-admin/facility-add-admin-sign/facility-add-admin-sign.component';
import { FacilitiesAdminComponent } from './facilities-admin/facilities-admin.component';
import { FacilitiesEditComponent } from './facilities-edit/facilities-edit.component';
import {FacilitiesEnvironmentPipe} from './facilities-environment.pipe';
import {MatTabsModule} from '@angular/material/tabs';
import {MatDialogModule} from '@angular/material/dialog';
import {MatPaginatorIntl, MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {TranslateService} from "@ngx-translate/core";
import { PaginatorI18n } from '../core/parts/paginatorI18n';
import {FacilitiesDetailClientSecretDialogComponent} from "./facilities-detail/facilities-detail-client-secret-dialog/facilities-detail-client-secret-dialog.component";
import {FacilitiesRemoveAdminDialogComponent} from "./facilities-detail/facilities-remove-admin-dialog/facilities-remove-admin-dialog.component";

@NgModule({
  imports: [
    CommonModule,
    FacilitiesRoutingModule,
    SharedModule,
    MatTabsModule,
    MatDialogModule,
    MatSortModule,
    MatPaginatorModule
  ],
  declarations: [
    FacilitiesUserComponent,
    FacilitiesDetailComponent,
    FacilitiesAttributeValuePipe,
    FacilityMoveToProductionComponent,
    FacilitiesEditComponent,
    FacilityMoveToProductionComponent,
    FacilitiesDetailDeleteDialogComponent,
    FacilitiesRemoveAdminDialogComponent,
    FacilitiesDetailClientSecretDialogComponent,
    FacilityAddAdminComponent,
    FacilityAddAdminSignComponent,
    FacilitiesAdminComponent,
    FacilitiesEnvironmentPipe
  ],
  providers: [
    {
      provide: MatPaginatorIntl, deps: [TranslateService],
      useFactory: (translateService: TranslateService) => new PaginatorI18n(translateService).getPaginatorIntl()
    }
  ]

})
export class FacilitiesModule { }
