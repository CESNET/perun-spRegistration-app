import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FacilitiesService} from '../../../../core/services/facilities.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import {LinkCode} from "../../../../core/models/LinkCode";
import {Facility} from "../../../../core/models/Facility";
import {DetailViewItem} from "../../../../core/models/items/DetailViewItem";

@Component({
  selector: 'app-facility-add-admin-sign',
  templateUrl: './facility-add-admin-sign.component.html',
  styleUrls: ['./facility-add-admin-sign.component.scss']
})
export class FacilityAddAdminSignComponent implements OnInit, OnDestroy {

  constructor(
    private route: ActivatedRoute,
    private facilitiesService: FacilitiesService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  private sub: Subscription;
  loading = true;

  private hash: string;
  details: LinkCode;
  facility: Facility;
  serviceAttrs: DetailViewItem[] = [];
  orgAttrs: DetailViewItem[] = [];

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      this.hash = params.code;
      this.facilitiesService.addAdminGetDetails(this.hash).subscribe(details => {
        if (details) {
          this.details = new LinkCode(details);
          this.facilitiesService.addAdminGetFacilityDetails(this.details.facilityId).subscribe(response => {
            this.facility = new Facility(response);
            this.mapAttributes();
            this.loading = false;
          });
        } else {
          this.router.navigate(['/notFound']);
          this.loading = false;
        }
      });
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  private mapAttributes() {
    this.facility.serviceAttrs().forEach((attr, _) => {
      this.serviceAttrs.push(new DetailViewItem(attr));
    });
    this.facility.organizationAttrs().forEach((attr, _) => {
      this.orgAttrs.push(new DetailViewItem(attr));
    });

    this.serviceAttrs = FacilityAddAdminSignComponent.sortItems(this.serviceAttrs);
    this.orgAttrs = FacilityAddAdminSignComponent.sortItems(this.orgAttrs);
  }

  private static sortItems(items: DetailViewItem[]): DetailViewItem[] {
    items.sort((a, b) => {
      return a.position - b.position;
    });

    return items;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  addAdminConfirm(): void {
    this.facilitiesService.addAdminConfirm(this.hash).subscribe(response => {
      if (response) {
        this.translate.get('FACILITIES.ADD_ADMIN_SUCCESS').subscribe(successMessage => {
          this.snackBar.open(successMessage, null, {duration: 5000});
          this.router.navigate(['/auth']);
        });
      } else {
        this.router.navigate(['/notFound']);
      }
    });
  }

  addAdminReject(): void {
    this.facilitiesService.addAdminReject(this.hash).subscribe(response => {
      if (response) {
        this.translate.get('FACILITIES.REJ_ADMIN_SUCCESS').subscribe(successMessage => {
          const snackBarRef = this.snackBar.open(successMessage, null, {duration: 5000});
          this.router.navigate(['/auth']);
        });
      } else {
        this.router.navigate(['/notFound']);
      }
    });
  }

}
