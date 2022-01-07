import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { FacilitiesService } from '../../core/services/facilities.service';
import { ProvidedService } from "../../core/models/ProvidedService";
import {AppComponent} from "../../app.component";

@Component({
  selector: 'app-all-facilities',
  templateUrl: './facilities-admin.component.html',
  styleUrls: ['./facilities-admin.component.scss']
})
export class FacilitiesAdminComponent implements OnInit, OnDestroy {

  private paginator: MatPaginator = undefined;
  private sort: MatSort = undefined;
  private paginator2: MatPaginator = undefined;
  private sort2: MatSort = undefined;
  private servicesSubscription: Subscription;
  private externalServicesSubscription: Subscription;

  constructor(
    private facilitiesService: FacilitiesService,
    private translate: TranslateService
  ) {
    this.externalServicesEnabled = AppComponent.pageConfig.externalServices;
  }

  @ViewChild('paginator1', {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setServicesDataSource();
  }

  @ViewChild('sort1', {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setServicesDataSource();
  }

  @ViewChild('paginator2', {static: false}) set matPaginator2(mp: MatPaginator) {
    this.paginator2 = mp;
    this.setExternalServicesDataSource();
  }

  @ViewChild('sort2', {static: false}) set matSort2(ms: MatSort) {
    this.sort2 = ms;
    this.setExternalServicesDataSource();
  }

  loading = false;
  isLoadingTable1 = true;
  isLoadingTable2 = true;
  displayedColumns: string[] = ['facilityId', 'name', 'description', 'identifier', 'environment', 'protocol', 'deleted'];
  displayedColumnsExtServices: string[] = ['facilityId', 'name', 'description', 'identifier', 'environment', 'protocol'];
  services: ProvidedService[] = [];
  externalServices: ProvidedService[] = [];
  servicesDataSource: MatTableDataSource<ProvidedService> = new MatTableDataSource<ProvidedService>();
  externalServicesDataSource: MatTableDataSource<ProvidedService> = new MatTableDataSource<ProvidedService>();
  externalServicesEnabled: boolean;

  ngOnInit() {
    this.servicesSubscription = this.facilitiesService.getAllFacilities().subscribe(services => {
      this.services = services.map(s => new ProvidedService(s));
      this.setServicesDataSource();
      this.isLoadingTable1 = false;
    }, _ => {
      this.isLoadingTable1 = false;
    });
    if (this.externalServicesEnabled) {
      this.externalServicesSubscription = this.facilitiesService.getAllExternalFacilities().subscribe(services => {
        this.externalServices = services.map(s => new ProvidedService(s));
        this.setExternalServicesDataSource();
        this.isLoadingTable2 = false;
      }, _ => {
        this.isLoadingTable2 = false;
      });
    }
  }

  ngOnDestroy() {
    if (this.servicesSubscription) {
      this.servicesSubscription.unsubscribe();
    }

    if (this.externalServicesSubscription) {
      this.externalServicesSubscription.unsubscribe();
    }
  }

  setServicesDataSource(): void {
    if (this.servicesDataSource) {
      this.servicesDataSource.data = this.services;
      this.servicesDataSource.sort = this.sort;
      this.servicesDataSource.paginator = this.paginator;
      this.setSorting(this.servicesDataSource);
      this.setFiltering(this.servicesDataSource);
    }
  }

  setExternalServicesDataSource(): void {
    if (this.externalServicesDataSource) {
      this.externalServicesDataSource.data = this.externalServices;
      this.externalServicesDataSource.sort = this.sort2;
      this.externalServicesDataSource.paginator = this.paginator2;
      this.setSorting(this.externalServicesDataSource);
      this.setFiltering(this.externalServicesDataSource);
    }
  }

  doFilter(value: string): void {
    if (this.servicesDataSource) {
      value = value ? value.trim().toLowerCase(): '';
      this.servicesDataSource.filter = value;
    }
    if (this.externalServicesDataSource) {
      value = value ? value.trim().toLowerCase(): '';
      this.externalServicesDataSource.filter = value;
    }
  }

  private setSorting(dataSource): void {
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
        case 'identifier': return data.identifier;
        case 'environment': return data.environment;
        case 'protocol': return data.protocol;
        case 'facilityId':
        default:
          return data.facilityId;
      }
    });
  }

  private setFiltering(dataSource): void {
    if (!dataSource) {
      return;
    }
    dataSource.filterPredicate = ((data: ProvidedService, filter: string) => {
      if (!filter) {
        return true;
      }
      const id = data.facilityId.toString();
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
      const identifier = data.identifier.replace(/\s/g, '').toLowerCase();

      const parts = filter.split(' ');
      for (let part of parts) {
        if (!(id.includes(part) || name.includes(part) || desc.includes(part) || protocol.includes(part)
          || env.includes(part) || identifier.includes(part))) {
          return false;
        }
      }
      return true;
    });
  }

}
