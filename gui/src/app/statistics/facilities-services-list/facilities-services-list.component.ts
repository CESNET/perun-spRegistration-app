import { Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { FacilitiesService } from '../../core/services/facilities.service';
import { Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { TranslateService } from '@ngx-translate/core';
import {ProvidedServiceOverview} from '../../core/models/ProvidedServiceOverview';
import {ProvidedServicesOverview} from '../../core/models/ProvidedServicesOverview';
import {AppComponent} from '../../app.component';
import {ConfService} from '../../shared/conf-service';
import {ConfigService} from '../../core/services/config.service';

const PRODUCTION = 'PRODUCTION';
const STAGING = 'STAGING';
const TESTING = 'TESTING';

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './facilities-services-list.component.html',
  styleUrls: ['./facilities-services-list.component.scss']
})
export class FacilitiesServicesListComponent implements OnInit, OnDestroy {

  displayedColumns: string[] = ['name', 'description', 'protocol'];

  servicesOverview: ProvidedServicesOverview = new ProvidedServicesOverview(null);

  productionServicesDataSource: MatTableDataSource<ProvidedServiceOverview> =
    new MatTableDataSource<ProvidedServiceOverview>([]);
  stagingServicesDataSource: MatTableDataSource<ProvidedServiceOverview> =
    new MatTableDataSource<ProvidedServiceOverview>([]);
  testingServicesDataSource: MatTableDataSource<ProvidedServiceOverview> =
    new MatTableDataSource<ProvidedServiceOverview>([]);

  isLoadingTable = true;

  overviewTableData = [];
  colorScheme = [
    { name: 'SAML - PROD', value: '#FF0000' },
    { name: 'SAML - STAGE', value: '#FF6666' },
    { name: 'SAML - TEST', value: '#FFCCCC' },
    { name: 'OIDC - PROD', value: '#0000FF' },
    { name: 'OIDC - STAGE', value: '#6666FF' },
    { name: 'OIDC - TEST', value: '#CCCCFF' }
  ];

  enabledProtocols: string[] = [];
  enabledEnvironments: string[] = [];

  private paginatorProduction: MatPaginator = undefined;
  private sortProduction: MatSort = undefined;

  private paginatorStaging: MatPaginator = undefined;
  private sortStaging: MatSort = undefined;

  private paginatorTesting: MatPaginator = undefined;
  private sortTesting: MatSort = undefined;

  private servicesOverviewSubscription: Subscription;
  private protocolsSubscription: Subscription;
  private environmentsSubscription: Subscription;

  constructor(
    private facilitiesService: FacilitiesService,
    private translate: TranslateService,
    private confService: ConfService,
    private configService: ConfigService
  ) {
    if (confService.statisticsDisplayedAttributes) {
      this.displayedColumns = confService.statisticsDisplayedAttributes;
    }
  }

  @ViewChild('paginatorProduction', {static: false}) set matPaginatorProduction(mp: MatPaginator) {
    this.paginatorProduction = mp;
    this.setProductionServicesDataSource();
  }
  @ViewChild('sortProduction', {static: false}) set matSortProduction(ms: MatSort) {
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

  private static extractData(map: Map<string, number>, key: string): number {
    if (map && map.has(key)) {
      return map.get(key);
    }
    return null;
  }

  private static pushData(data: object[], servicesCount: number, label: string) {
    if (servicesCount === null || servicesCount === undefined) {
      return;
    }
    data.push({ name: label, value: servicesCount });
  }

  ngOnInit() {
    this.protocolsSubscription = this.configService.getProtocolsEnabled().subscribe(protocols => {
      this.enabledProtocols = protocols;
      this.environmentsSubscription = this.configService.getEnvironments().subscribe(environments => {
        this.enabledEnvironments = environments;
        this.servicesOverviewSubscription = this.facilitiesService.getProvidedServicesOverview().subscribe(overview => {
          this.servicesOverview = new ProvidedServicesOverview(overview);
          this.setDataSources();
          this.setOverviewTableData();
          this.isLoadingTable = false;
        });
      });
    });
  }

  ngOnDestroy() {
    if (this.servicesOverviewSubscription) {
      this.servicesOverviewSubscription.unsubscribe();
    }
    if (this.environmentsSubscription) {
      this.environmentsSubscription.unsubscribe();
    }
    if (this.protocolsSubscription) {
      this.protocolsSubscription.unsubscribe();
    }
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

  hasDisplayableProductionServices() {
    return this.servicesOverview && this.servicesOverview.displayableProductionServices;
  }

  hasDisplayableStagingServices() {
    return this.servicesOverview && this.servicesOverview.displayableStagingServices && AppComponent.isApplicationAdmin();
  }

  hasDisplayableTestingServices() {
    return this.servicesOverview && this.servicesOverview.displayableTestingServices && AppComponent.isApplicationAdmin();
  }

  axisFormat(val) {
    if (val % 1 === 0) {
      return val.toLocaleString();
    } else {
      return '';
    }
  }

  hasProductionEnabled() {
    return this.enabledEnvironments.indexOf('production') !== -1;
  }

  hasStagingEnabled() {
    return this.enabledEnvironments.indexOf('staging') !== -1;
  }

  hasTestingEnabled() {
    return this.enabledEnvironments.indexOf('testing') !== -1;
  }

  hasSamlEnabled() {
    return this.enabledProtocols.indexOf('SAML') !== -1;
  }

  hasOidcEnabled() {
    return this.enabledProtocols.indexOf('OIDC') !== -1;
  }

  getSamlProductionCount(): number {
    if (!this.hasProductionEnabled() || !this.hasSamlEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, PRODUCTION);
  }

  getSamlStagingCount(): number {
    if (!this.hasStagingEnabled() || !this.hasSamlEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, STAGING);
  }

  getSamlTestingCount(): number {
    if (!this.hasTestingEnabled() || !this.hasSamlEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, TESTING);
  }

  getOidcProductionCount(): number {
    if (!this.hasProductionEnabled() || !this.hasOidcEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, PRODUCTION);
  }

  getOidcStagingCount(): number {
    if (!this.hasStagingEnabled() || !this.hasOidcEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, STAGING);
  }

  getOidcTestingCount(): number {
    if (!this.hasTestingEnabled() || !this.hasOidcEnabled() || !this.servicesOverview) {
      return null;
    }
    return FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, TESTING);
  }

  getProductionCount(): number {
    let val = null;
    if (!this.hasProductionEnabled()) {
      return null;
    }
    if (this.hasSamlEnabled()) {
      val = this.getSamlProductionCount();
    }
    if (this.hasOidcEnabled()) {
      if (val === null || val === undefined) {
        val = this.getOidcProductionCount();
      } else {
        val += this.getOidcProductionCount();
      }
    }
    return val;
  }

  getStagingCount(): number {
    let val = null;
    if (!this.hasStagingEnabled()) {
      return null;
    }
    if (this.hasSamlEnabled()) {
      val = this.getSamlStagingCount();
    }
    if (this.hasOidcEnabled()) {
      if (val === null || val === undefined) {
        val = this.getOidcStagingCount();
      } else {
        val += this.getOidcStagingCount();
      }
    }
    return val;
  }

  getTestingCount(): number {
    let val = null;
    if (!this.hasTestingEnabled()) {
      return null;
    }
    if (this.hasSamlEnabled()) {
      val = this.getSamlTestingCount();
    }
    if (this.hasOidcEnabled()) {
      if (val === null || val === undefined) {
        val = this.getOidcTestingCount();
      } else {
        val += this.getOidcTestingCount();
      }
    }
    return val;
  }

  chartClicked(e: any): void { }

  chartHovered(e: any): void { }

  isApplicationAdmin(): boolean {
    return AppComponent.isApplicationAdmin();
  }

  private setDataSources(): void {
    this.setProductionServicesDataSource();
    this.setStagingServicesDataSource();
    this.setTestingServicesDataSource();
  }

  private setProductionServicesDataSource(): void {
    if (!this.hasProductionEnabled()) {
      return;
    }
    this.setServicesDataSource(
      this.productionServicesDataSource,
      this.servicesOverview.productionServices,
      this.sortProduction,
      this.paginatorProduction
    )
  }

  private setStagingServicesDataSource(): void {
    if (!this.hasStagingEnabled()) {
      return;
    }
    this.setServicesDataSource(
      this.stagingServicesDataSource,
      this.servicesOverview.stagingServices,
      this.sortStaging,
      this.paginatorStaging
    );
  }

  private setTestingServicesDataSource(): void {
    if (!this.hasTestingEnabled()) {
      return;
    }
    this.setServicesDataSource(
      this.testingServicesDataSource,
      this.servicesOverview.testingServices,
      this.sortTesting,
      this.paginatorTesting
    );
  }

  private setServicesDataSource(datasource: MatTableDataSource<ProvidedServiceOverview>,
                                data: ProvidedServiceOverview[],
                                sort: MatSort, paginator: MatPaginator): void
  {
    if (datasource) {
      datasource.data = data;
      datasource.sort = sort;
      datasource.paginator = paginator;

      this.setSorting(datasource);
      this.setFiltering(datasource);
    }
  }

  private setSorting(dataSource: MatTableDataSource<ProvidedServiceOverview>) {
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

  private setFiltering(dataSource: MatTableDataSource<ProvidedServiceOverview>) {
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
      for (const part of parts) {
        if (name.includes(part) || desc.includes(part) || protocol.includes(part) || env.includes(part)) {
          return true;
        }
      }
      return false;
    });
  }

  private setOverviewTableData() {
    if (!this.servicesOverview) {
      return;
    }
    const data = [];
    if (this.servicesOverview.samlServicesCount) {
      if (this.hasProductionEnabled()) {
        const productionSamlCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, PRODUCTION);
        FacilitiesServicesListComponent.pushData(data, productionSamlCount, 'SAML - PROD');
      }
      if (this.hasStagingEnabled()) {
        const stagingSamlCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, STAGING);
        FacilitiesServicesListComponent.pushData(data, stagingSamlCount, 'SAML - STAGE');
      }
      if (this.hasTestingEnabled()) {
        const testingSamlCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.samlServicesCount, TESTING);
        FacilitiesServicesListComponent.pushData(data, testingSamlCount, 'SAML - TEST');
      }
    }
    if (this.servicesOverview.oidcServicesCount) {
      if (this.hasProductionEnabled()) {
        const productionOidcCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, PRODUCTION);
        FacilitiesServicesListComponent.pushData(data, productionOidcCount, 'OIDC - PROD');
      }
      if (this.hasStagingEnabled()) {
        const stagingOidcCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, STAGING);
        FacilitiesServicesListComponent.pushData(data, stagingOidcCount, 'OIDC - STAGE');
      }
      if (this.hasTestingEnabled()) {
        const testingOidcCount = FacilitiesServicesListComponent.extractData(this.servicesOverview.oidcServicesCount, TESTING);
        FacilitiesServicesListComponent.pushData(data, testingOidcCount, 'OIDC - TEST');
      }
    }
    this.overviewTableData = data;
  }

}
