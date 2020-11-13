import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FacilitiesService} from '../core/services/facilities.service';
import {Subscription} from 'rxjs';
import {Facility} from '../core/models/Facility';
import { MatSnackBar } from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {FacilityDetailUserItem} from "../core/models/items/FacilityDetailUserItem";
import {User} from "../core/models/User";
import {DetailViewItem} from "../core/models/items/DetailViewItem";

@Component({
  selector: 'app-document-sign',
  templateUrl: './document-sign.component.html',
  styleUrls: ['./document-sign.component.scss']
})
export class DocumentSignComponent implements OnInit, OnDestroy {
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

  facility: Facility;
  facilityAttrsService: DetailViewItem[] = [];
  facilityAttrsOrganization: DetailViewItem[] = [];
  facilityAdmins: FacilityDetailUserItem[] = [];

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      if (params.code) {
        this.hash = params.code;
        this.facilitiesService.getRequestDetailsWithHash(this.hash).subscribe(request => {
          this.facilitiesService.getFacilitySignature(request.facilityId).subscribe(facility => {
            this.facility = new Facility(facility);
            this.mapAttributes();
            this.mapAdmins();
            this.loading = false;
          }, error => {
            this.loading = false;
            console.log(error);
          });
        });
      } else {
        this.router.navigate(['/notFound']);
      }
    });
  }

  private mapAttributes() {
    this.facility.serviceAttrs().forEach((attr, _) => {
      this.facilityAttrsService.push(new DetailViewItem(attr));
    });

    this.facility.organizationAttrs().forEach((attr, _) => {
      this.facilityAttrsOrganization.push(new DetailViewItem(attr));
    });
  }

  private mapAdmins() {
    this.facility.managers.forEach(user => {
      this.facilityAdmins.push(new FacilityDetailUserItem(user));
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  approveRequest(): void {
    this.facilitiesService.approveTransferToProduction(this.hash).subscribe(_ => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
          this.snackBar.open(successMessage, null, {duration: 5000});
          this.router.navigate(['/auth']);
      });
    });
  }

  rejectRequest(): void {
    this.facilitiesService.rejectTransferToProduction(this.hash).subscribe(_ => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
        this.snackBar.open(successMessage, null, {duration: 5000});
        this.router.navigate(['/auth']);
      });
    });
  }

  isUndefined(value) {
    // TODO: extract to one common method, also used in request-detail
    if (value === undefined || value === null) {
      return true;
    } else {
      if (value instanceof Array || value instanceof String) {
        return value.length === 0;
      } else if (value instanceof Object) {
        return value.constructor === Object && Object.entries(value).length === 0;
      }

      return false;
    }
  }

}
