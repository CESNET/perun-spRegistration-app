import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FacilitiesService} from '../../core/services/facilities.service';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {Subscription} from 'rxjs';
import {MatPaginator} from '@angular/material/paginator';
import {ProvidedService} from "../../core/models/ProvidedService";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-all-facilities',
  templateUrl: './all-facilities.component.html',
  styleUrls: ['./all-facilities.component.scss']
})
export class AllFacilitiesComponent implements OnInit, OnDestroy {

  private sort: MatSort;
  private paginator: MatPaginator;
  private facilitiesSubscription: Subscription;

  constructor(private facilitiesService: FacilitiesService,
              private translate: TranslateService) {
    this.facilities = [];
    this.dataSource = new MatTableDataSource<ProvidedService>([]);
  }

  @Input() facilities: ProvidedService[];

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  displayedColumns: string[] = ['id', 'name', 'description', 'identifier', 'environment', 'protocol'];
  dataSource: MatTableDataSource<ProvidedService>;
  loading = true;

  setDataSource() {
    if (!!this.dataSource) {
      if (!!this.sort) {
        this.dataSource.sort = this.sort;
        this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
          switch (sortHeaderId) {
            case 'id': return data.id;
            case 'name': {
              if (!!data.name && data.name.has(this.translate.currentLang)) {
                return data.name.get(this.translate.currentLang).toLowerCase();
              } else {
                return "";
              }
            }
            case 'description': {
              if (!!data.description && data.description.has(this.translate.currentLang)) {
                return data.description.get(this.translate.currentLang).toLowerCase();
              } else {
                return "";
              }
            }
            case 'identifier': return data.identifier;
            case 'environment': return data.environment;
            case 'protocol': return data.protocol;
          }
        });
      }
      if (!!this.paginator) {
        this.dataSource.paginator = this.paginator;
      }
    }
  }

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getAllFacilities().subscribe(facilities => {
      this.loading = false;
      this.facilities = facilities.map(f => new ProvidedService(f));
      this.dataSource = new MatTableDataSource<ProvidedService>(this.facilities);
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  ngOnDestroy() {
    this.facilitiesSubscription.unsubscribe();
  }

  doFilter = (value: string) => {
    this.dataSource.filter = value.trim().toLocaleLowerCase();
  }
}
