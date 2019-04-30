import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FacilitiesService} from "../../core/services/facilities.service";
import {Facility} from "../../core/models/Facility";
import {MatSort, MatTableDataSource} from "@angular/material";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-all-facilities',
  templateUrl: './all-facilities.component.html',
  styleUrls: ['./all-facilities.component.scss']
})
export class AllFacilitiesComponent implements OnInit, OnDestroy {

  constructor(private facilitiesService: FacilitiesService) { }

  @Input()
  facilities: Facility[];

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
    this.facilitiesSubscription = this.facilitiesService.getAllFacilities().subscribe(facilities => {
      this.loading = false;
      this.facilities = facilities;
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
