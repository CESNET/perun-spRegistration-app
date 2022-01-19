import { Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { FacilitiesService } from '../../core/services/facilities.service';
import { Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from "@angular/material/paginator";
import { TranslateService } from '@ngx-translate/core';
import {ProvidedServiceOverview} from "../../core/models/ProvidedServiceOverview";
import {ProvidedServicesOverview} from "../../core/models/ProvidedServicesOverview";
import {AppComponent} from "../../app.component";

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './facilities-services-list.component.html',
  styleUrls: ['./facilities-services-list.component.scss']
})
export class FacilitiesServicesListComponent implements OnInit, OnDestroy {

  private paginatorProduction: MatPaginator = undefined;
  private sortProduction: MatSort = undefined;

  private paginatorStaging: MatPaginator = undefined;
  private sortStaging: MatSort = undefined;

  private paginatorTesting: MatPaginator = undefined;
  private sortTesting: MatSort = undefined;

  private servicesOverviewSubscription: Subscription

  displayedColumns: string[] = ['name', 'description', 'protocol', 'environment'];

  @ViewChild('paginatorProduction', {static: false}) set matPaginatorProduction(mp: MatPaginator) {
    this.paginatorProduction = mp;
    this.setProductionServicesDataSource();
  }
  @ViewChild('sortPorduction', {static: false}) set matSortProduction(ms: MatSort) {
    this.sortProduction = ms;
    this.setProductionServicesDataSource();
  }

  @ViewChild('paginatorStaging', {static: false}) set matPaginatorStaging(mp: MatPaginator) {
    this.paginatorStaging = mp;
    this.setStagingServicesDataSource();
  }
  @ViewChild('sortStaging', {static: false}) set matSortStaging(ms: MatSort) {
    this.sortStaging = ms;
    this.setStagingServicesDataSource();
  }

  @ViewChild('paginatorTesting', {static: false}) set matPaginatorTesting(mp: MatPaginator) {
    this.paginatorTesting = mp;
    this.setTestingServicesDataSource();
  }
  @ViewChild('sortTesting', {static: false}) set matSortTesting(ms: MatSort) {
    this.sortTesting = ms;
    this.setTestingServicesDataSource();
  }

  constructor(
    private facilitiesService: FacilitiesService,
    private translate: TranslateService
  ) {
  }

  servicesOverview: ProvidedServicesOverview = new ProvidedServicesOverview(null);

  productionServicesDataSource: MatTableDataSource<ProvidedServiceOverview> = new MatTableDataSource<ProvidedServiceOverview>();
  stagingServicesDataSource: MatTableDataSource<ProvidedServiceOverview> = new MatTableDataSource<ProvidedServiceOverview>();
  testingServicesDataSource: MatTableDataSource<ProvidedServiceOverview> = new MatTableDataSource<ProvidedServiceOverview>();

  isLoadingTable = true;

  overviewTableData = [];
  colorScheme = [
      { name: "SAML - PROD", value: "#FF0000" },
      { name: "SAML - STAGE", value: "#FF6666" },
      { name: "SAML - TEST", value: "#FFCCCC" },
      { name: "OIDC - PROD", value: "#0000FF" },
      { name: "OIDC - STAGE", value: "#6666FF" },
      { name: "OIDC - TEST", value: "#CCCCFF" }
  ];

  ngOnInit() {
    this.servicesOverviewSubscription = this.facilitiesService.getProvidedServicesOverview().subscribe(overview => {
      this.servicesOverview = new ProvidedServicesOverview(overview);

      this.setServicesDataSource(
        this.productionServicesDataSource,
        this.servicesOverview.productionServices,
        this.sortProduction,
        this.paginatorProduction
      );

      if (this.servicesOverview.stagingServices) {
        this.setServicesDataSource(
          this.stagingServicesDataSource,
          this.servicesOverview.stagingServices,
          this.sortStaging,
          this.paginatorStaging
        );
      }

      if (this.servicesOverview.testingServices) {
        this.setServicesDataSource(
          this.testingServicesDataSource,
          this.servicesOverview.testingServices,
          this.sortTesting,
          this.paginatorTesting
        );
      }

      this.overviewTableData = [
        { name: "SAML - PROD", value: this.servicesOverview.samlServicesCount['PRODUCTION'] },
        { name: "SAML - STAGE", value: this.servicesOverview.samlServicesCount['STAGING'] },
        { name: "SAML - TEST", value: this.servicesOverview.samlServicesCount['TESTING'] },
        { name: "OIDC - PROD", value: this.servicesOverview.oidcServicesCount['PRODUCTION'] },
        { name: "OIDC - STAGE", value: this.servicesOverview.oidcServicesCount['STAGING'] },
        { name: "OIDC - TEST", value: this.servicesOverview.oidcServicesCount['TESTING'] }
      ]

      this.isLoadingTable = false;
    });
  }

  ngOnDestroy() {
    if (this.servicesOverviewSubscription) {
      this.servicesOverviewSubscription.unsubscribe();
    }
  }

  setProductionServicesDataSource(): void {
    this.setServicesDataSource(
      this.productionServicesDataSource,
      this.servicesOverview.productionServices,
      this.sortProduction,
      this.paginatorProduction
    )
  }

  setStagingServicesDataSource(): void {
    if (this.productionServicesDataSource) {
      this.setServicesDataSource(
        this.stagingServicesDataSource,
        this.servicesOverview.stagingServices,
        this.sortStaging,
        this.paginatorStaging
      );
    }
  }

  setTestingServicesDataSource(): void {
      this.setServicesDataSource(
        this.testingServicesDataSource,
        this.servicesOverview.testingServices,
        this.sortTesting,
        this.paginatorTesting
      );
  }

  setServicesDataSource(datasource: MatTableDataSource<ProvidedServiceOverview>,
                        data: ProvidedServiceOverview[],
                        sort: MatSort,
                        paginator: MatPaginator): void {
    if (datasource) {
      datasource.data = data;
      datasource.sort = sort;
      datasource.paginator = paginator;

      this.setSorting(datasource);
      this.setFiltering(datasource);
    }
  }

  private setSorting(dataSource) {
    if (!dataSource) {
      return;
    }
    dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'name': {
          if (data.name && data.name.has(this.translate.currentLang)) {
            return data.name.get(this.translate.currentLang).toLowerCase();
          } else {
            return '';
          }
        }
        case 'description': {
          if (data.description && data.description.has(this.translate.currentLang)) {
            return data.description.get(this.translate.currentLang).toLowerCase();
          } else {
            return '';
          }
        }
        case 'environment': return data.environment;
        case 'protocol': return data.protocol;
      }
    });
  }

  private setFiltering(dataSource) {
    if (!dataSource) {
      return;
    }
    dataSource.filterPredicate = ((data: ProvidedServiceOverview, filter: string) => {
      if (!filter) {
        return true;
      }
      let name = '';
      if (data.name && data.name.has(this.translate.currentLang)) {
        name = data.name.get(this.translate.currentLang).replace(/\s/g, '').toLowerCase();
      }
      let desc = '';
      if (data.description && data.description.has(this.translate.currentLang)) {
        desc = data.description.get(this.translate.currentLang).replace(/\s/g, '').toLowerCase();
      }
      const protocol = data.protocol.replace(/\s/g, '').toLowerCase();
      const env = data.environment.replace(/\s/g, '').toLowerCase();

      const parts = filter.split(' ');
      for (let part of parts) {
        if (name.includes(part) || desc.includes(part) || protocol.includes(part) || env.includes(part)) {
          return true;
        }
      }
      return false;
    });
  }

  doFilterProduction(value: string): void {
    if (this.productionServicesDataSource) {
      value = value ? value.trim().toLowerCase(): '';
      this.productionServicesDataSource.filter = value;
    }
  }

  doFilterStaging(value: string): void {
    if (this.productionServicesDataSource) {
      value = value ? value.trim().toLowerCase() : '';
      this.productionServicesDataSource.filter = value;
    }
  }

  doFilterTesting(value: string): void {
    if (this.productionServicesDataSource) {
      value = value ? value.trim().toLowerCase() : '';
      this.productionServicesDataSource.filter = value;
    }
  }

  hasDisplayableStagingServices() {
    return this.servicesOverview && this.servicesOverview.displayableStagingServices && AppComponent.isApplicationAdmin();
  }

  hasDisplayableTestingServices() {
    return this.servicesOverview && this.servicesOverview.displayableTestingServices && AppComponent.isApplicationAdmin();
  }

  public chartClicked(e: any): void {
  }

  public chartHovered(e: any): void {
  }

  axisFormat(val) {
    if (val % 1 === 0) {
      return val.toLocaleString();
    } else {
      return '';
    }
  }

}
