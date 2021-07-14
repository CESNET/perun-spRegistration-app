import { Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { FacilitiesService } from '../../core/services/facilities.service';
import { Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from "@angular/material/paginator";
import { ProvidedService } from "../../core/models/ProvidedService";
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './facilities-user.component.html',
  styleUrls: ['./facilities-user.component.scss']
})
export class FacilitiesUserComponent implements OnInit, OnDestroy {

  private paginator: MatPaginator = undefined;
  private sort: MatSort = undefined;
  private facilitiesSubscription: Subscription;

  constructor(
    private facilitiesService: FacilitiesService,
    private translate: TranslateService
  ) { }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  loading = true;
  displayedColumns: string[] = ['facilityId', 'name', 'description', 'identifier', 'environment', 'protocol'];
  services: ProvidedService[] = [];
  dataSource: MatTableDataSource<ProvidedService> = new MatTableDataSource<ProvidedService>();

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getMyFacilities().subscribe(services => {
      this.services = services.map(s => new ProvidedService(s));
      this.setDataSource();
      this.loading = false;
    }, _ => {
      this.loading = false;
    });
  }

  ngOnDestroy() {
    this.facilitiesSubscription.unsubscribe();
  }

  setDataSource(): void {
    if (this.dataSource) {
      this.dataSource.data = this.services;
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.setSorting();
      this.setFiltering();
    }
  }

  doFilter(value: string): void {
    if (this.dataSource) {
      value = value ? value.trim().toLowerCase(): '';
      this.dataSource.filter = value;
    }
  }

  private setSorting() {
    if (!this.dataSource) {
      return;
    }
    this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'facilityId': return data.id;
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
    if (!this.dataSource) {
      return;
    }
    this.dataSource.filterPredicate = ((data: ProvidedService, filter: string) => {
      if (!filter) {
        return true;
      }
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
