import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Facility } from "../../core/models/Facility";
import { FacilitiesService } from "../../core/services/facilities.service";
import { Subscription } from "rxjs";
import {MatSort, MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-facilities-overview',
  templateUrl: './services-overview.component.html',
  styleUrls: ['./services-overview.component.scss']
})
export class ServicesOverviewComponent implements OnInit, OnDestroy {

  constructor(private facilitiesService: FacilitiesService) { }

  @Input()
  myFacilities: Facility[];

  @ViewChild(MatSort) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  displayedColumns: string[] = ['id', 'name', 'description'];
  dataSource: MatTableDataSource<Facility>;
  loading = true;

  private sort: MatSort;
  private facilitiesSubscription: Subscription;

  setDataSource() {
    if (!!this.dataSource) {
      this.dataSource.sort = this.sort;
    }
  }

  ngOnInit() {
    this.facilitiesSubscription = this.facilitiesService.getMyFacilities().subscribe(facilities => {
      this.loading = false;
      this.myFacilities = facilities;
      this.dataSource = new MatTableDataSource<Facility>(facilities);
    }, error => {
        this.loading = false;
        console.log(error);
    });
  }

  ngOnDestroy() {
    this.facilitiesSubscription.unsubscribe();
  }
}
