import { NgModule } from '@angular/core';
import {FacilitiesServicesListComponent} from "./facilities-services-list/facilities-services-list.component";
import {MatPaginatorIntl, MatPaginatorModule} from "@angular/material/paginator";
import {TranslateParser, TranslateService} from "@ngx-translate/core";
import {PaginatorI18n} from "../core/parts/paginatorI18n";
import {CommonModule} from "@angular/common";
import {SharedModule} from "../shared/shared.module";
import {MatSortModule} from "@angular/material/sort";
import {StatisticsRoutingModule} from "./statistics-routing.module";
import { NgxChartsModule } from '@swimlane/ngx-charts';

@NgModule({
  imports: [
    CommonModule,
    StatisticsRoutingModule,
    SharedModule,
    MatSortModule,
    MatPaginatorModule,
    NgxChartsModule
  ],
  declarations: [
    FacilitiesServicesListComponent
  ],
  providers: [
    {
      provide: MatPaginatorIntl, deps: [TranslateService, TranslateParser],
      useFactory: (translateService: TranslateService, translateParser: TranslateParser) => new PaginatorI18n(translateService, translateParser)
    }
  ]
})
export class StatisticsModule { }
