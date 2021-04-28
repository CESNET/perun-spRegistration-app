import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { FacilitiesService } from '../../core/services/facilities.service';
import { ProvidedService } from "../../core/models/ProvidedService";

@Component({
  selector: 'app-all-facilities',
  templateUrl: './facilities-admin.component.html',
  styleUrls: ['./facilities-admin.component.scss']
})
export class FacilitiesAdminComponent implements OnInit, OnDestroy {

  private sort: MatSort;
  private paginator: MatPaginator;
  private facilitiesSubscription: Subscription;

  constructor(
    private facilitiesService: FacilitiesService,
    private translate: TranslateService)
  {
    this.services = [];
    this.setDataSource();
  }

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  loading: boolean = true;
  displayedColumns: string[] = ['facilityId', 'name', 'description', 'identifier', 'environment', 'protocol'];
  services: ProvidedService[] = [];
  dataSource: MatTableDataSource<ProvidedService> = new MatTableDataSource<ProvidedService>(this.services);

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getAllFacilities().subscribe(services => {
      this.services = services.map(s => new ProvidedService(s));
      this.setDataSource()
      this.loading = false;
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  ngOnDestroy() {
    this.facilitiesSubscription.unsubscribe();
  }

  doFilter(value: string) {
    this.dataSource.filter = value.trim().toLowerCase();
  }

  private setDataSource() {
    this.dataSource = new MatTableDataSource<ProvidedService>(this.services);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.setSorting();
    this.setFiltering();
  }

  private setSorting() {
    this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'id': return data.id;
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
      }
    });
  }

  private setFiltering() {
    this.dataSource.filterPredicate = ((data: ProvidedService, filter: string) => {
      const id = data.id.toString();
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

      return id.includes(filter) || name.includes(filter) || desc.includes(filter) || protocol.includes(filter)
        || env.includes(filter) || identifier.includes(filter);
    });
  }

}
