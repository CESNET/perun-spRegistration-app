import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {FacilitiesService} from "../../core/services/facilities.service";
import {Facility} from "../../core/models/Facility";
import {AppComponent} from "../../app.component";
import {MatDialog} from "@angular/material";
import {FacilitiesDetailDialogComponent} from "./facilities-detail-dialog/facilities-detail-dialog.component";

export interface DialogData {
  parent: FacilitiesDetailComponent,
  facilityName: string
}

@Component({
  selector: 'app-facilities-detail',
  templateUrl: './facilities-detail.component.html',
  styleUrls: ['./facilities-detail.component.scss']
})
export class FacilitiesDetailComponent implements OnInit, OnDestroy {

  constructor(
    private route: ActivatedRoute,
    private facilitiesService: FacilitiesService,
    private router: Router,
    public dialog: MatDialog,
  ) { }

  private sub : Subscription;

  displayedColumns: string[] = ['fullname', 'value'];
  facilityAttributes: any[];

  loading = true;
  facility: Facility;

  isUserAdmin: boolean;

  private mapAttributes() {
    this.facilityAttributes = [];
      for (let urn of Object.keys(this.facility.attrs)) {
        let item = this.facility.attrs[urn];
        this.facilityAttributes.push(
          {
            "urn": urn,
            "value": item.value,
            "name": item.definition.displayName,
            "description": item.definition.description,
          }
        );
    }
  }

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.facilitiesService.getFacility(params['id']).subscribe(facility => {
        this.facility = facility;
        this.mapAttributes();
        this.isUserAdmin = AppComponent.isApplicationAdmin();
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });
    this.isUserAdmin = AppComponent.isApplicationAdmin();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  moveToProduction(): void {
    this.router.navigateByUrl('auth/facilities/moveToProduction/' + this.facility.id );
  }

  openDeleteDialog(): void {
    this.dialog.open(FacilitiesDetailDialogComponent, {
      width: '400px',
      data: {parent: this, facilityName: this.facility.name}
    });
  }

  deleteFacility(): void {
    this.facilitiesService.removeFacility(this.facility.id).subscribe(id => {
      this.router.navigateByUrl('auth/requests/detail/' + id);
    });
  }

  addFacilityAdmin():void{
    this.router.navigateByUrl('auth/facilities/addAdmin/' + this.facility.id );
  }

  isUndefined(value) {
    //TODO: extract to one common method, also used in request-detail
    if (value === undefined || value === null) {
      return true;
    } else {
      if (value instanceof Array || value instanceof String) {
        return value.length === 0;
      } else if (value instanceof Object) {
        return value.constructor === Object && Object.entries(value).length === 0
      }

      return false;
    }
  }
}
