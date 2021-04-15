import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FacilitiesService } from '../core/services/facilities.service';
import { Subscription } from 'rxjs';
import { Facility } from '../core/models/Facility';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { FacilityDetailUserItem } from "../core/models/items/FacilityDetailUserItem";
import { DetailViewItem } from "../core/models/items/DetailViewItem";

@Component({
  selector: 'app-document-sign',
  templateUrl: './document-sign.component.html',
  styleUrls: ['./document-sign.component.scss']
})
export class DocumentSignComponent implements OnInit, OnDestroy {

  private routeSub: Subscription = null;
  private requestDetailsSub: Subscription = null;
  private facilitySignatureSub: Subscription = null;
  private code: string = null;

  constructor(
    private route: ActivatedRoute,
    private fService: FacilitiesService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) { }

  loading = true;
  facility: Facility = null;
  facilityAttrsService: DetailViewItem[] = [];
  facilityAttrsOrganization: DetailViewItem[] = [];
  facilityAdmins: FacilityDetailUserItem[] = [];

  ngOnInit() {
    this.routeSub = this.route.queryParams.subscribe(params => {
      if (params.code) {
        this.code = params.code;
        this.requestDetailsSub = this.fService.getRequestDetailsWithHash(this.code).subscribe(request => {
            this.facilitySignatureSub = this.fService.getFacilitySignature(request.facilityId).subscribe(facility => {
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
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
    if (this.facilitySignatureSub) {
      this.facilitySignatureSub.unsubscribe();
    }
    if (this.requestDetailsSub) {
      this.requestDetailsSub.unsubscribe();
    }
  }

  approveRequest(): void {
    this.fService.approveTransferToProduction(this.code).subscribe(_ => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
          this.snackBar.open(successMessage, null, {duration: 5000});
          this.router.navigate(['/auth']);
      });
    });
  }

  rejectRequest(): void {
    this.fService.rejectTransferToProduction(this.code).subscribe(_ => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
        this.snackBar.open(successMessage, null, {duration: 5000});
        this.router.navigate(['/auth']);
      });
    });
  }

  isUndefined(value: any) {
    // TODO: extract to one common method, also used in request-detail
    if (!value) {
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
