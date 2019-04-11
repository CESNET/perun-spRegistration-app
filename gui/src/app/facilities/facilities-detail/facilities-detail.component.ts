import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {FacilitiesService} from "../../core/services/facilities.service";
import {Facility} from "../../core/models/Facility";
import {PerunAttribute} from "../../core/models/PerunAttribute";
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
  facilityAttributes: PerunAttribute[];

  loading = true;
  facility: Facility;

  isUserAdmin: boolean;

  private mapAttributes() {
    this.facilityAttributes = [];
      for (let urn of Object.keys(this.facility.attrs)) {
        let item = this.facility.attrs[urn];
        this.facilityAttributes.push(new PerunAttribute(item.value, item.definition.displayName));
    }
  }

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.facilitiesService.getFacility(params['id']).subscribe(facility => {
        this.facility = facility;
        this.mapAttributes();
        this.isUserAdmin = AppComponent.isUserAdmin();
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });
    this.isUserAdmin = AppComponent.isUserAdmin();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  moveToProduction(): void {
    this.router.navigateByUrl('facilities/moveToProduction/' + this.facility.id );
  }

  openDeleteDialog(): void {
    const dialogRef = this.dialog.open(FacilitiesDetailDialogComponent, {
      width: '250px',
      data: {parent: this, facilityName: this.facility.name}
    });
    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  deleteFacility(): void {
    this.facilitiesService.removeFacility(this.facility.id).subscribe(id => {
      this.router.navigateByUrl('requests/detail/' + id);
    });
  }

  editFacility(): void{

  }

  addFacilityAdmin():void{
    this.router.navigateByUrl('facilities/addAdmin/' + this.facility.id );
  }

  editAdmins(): void{

  }

}
