import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { FacilitiesService } from '../../core/services/facilities.service';
import { Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {MatPaginator} from "@angular/material/paginator";
import {ProvidedService} from "../../core/models/ProvidedService";

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './services-overview.component.html',
  styleUrls: ['./services-overview.component.scss']
})
export class ServicesOverviewComponent implements OnInit, OnDestroy {

  private sort: MatSort;
  private paginator: MatPaginator;
  private facilitiesSubscription: Subscription;

  constructor(private facilitiesService: FacilitiesService) {
    this.myFacilities = [];
    this.dataSource = new MatTableDataSource<ProvidedService>([]);
  }

  @Input() myFacilities: ProvidedService[];

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  displayedColumns: string[] = ['id', 'name', 'description', 'environment', 'protocol'];
  dataSource: MatTableDataSource<ProvidedService>;
  loading = true;

  setDataSource() {
    if (!!this.dataSource) {
      if (!!this.sort) {
        this.dataSource.sort = this.sort;
      }
      if (!!this.paginator) {
        this.dataSource.paginator = this.paginator;
      }
    }
  }

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getMyFacilities().subscribe(facilities => {
      this.loading = false;
      this.myFacilities = facilities.map(f => new ProvidedService(f));
      this.dataSource = new MatTableDataSource<ProvidedService>(this.myFacilities);
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
