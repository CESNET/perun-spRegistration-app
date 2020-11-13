import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FacilitiesService} from '../../core/services/facilities.service';
import {Facility} from '../../core/models/Facility';
import {AppComponent} from '../../app.component';
import { MatDialog } from '@angular/material/dialog';
import {FacilitiesDetailDeleteDialogComponent} from './facilities-detail-delete-dialog/facilities-detail-delete-dialog.component';
import {RequestsService} from '../../core/services/requests.service';
import {Subscription} from 'rxjs';
import {PerunAttribute} from "../../core/models/PerunAttribute";
import {User} from "../../core/models/User";
import {FacilityDetailUserItem} from "../../core/models/items/FacilityDetailUserItem";
import {DetailViewItem} from "../../core/models/items/DetailViewItem";
import {FacilitiesDetailClientSecretDialogComponent} from "./facilities-detail-client-secret-dialog/facilities-detail-client-secret-dialog.component";

export interface DialogData {
  parent: FacilitiesDetailComponent;
  facilityName: Map<string, string>;
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
    private requestsService: RequestsService,
    private router: Router,
    public dialog: MatDialog,
  ) { }

  private sub: Subscription;

  facilityAttrsService: DetailViewItem[] = [];
  facilityAttrsOrganization: DetailViewItem[] = [];
  facilityAttrsProtocol: DetailViewItem[] = [];
  facilityAttrsAccessControl: DetailViewItem[] = [];
  facilityAdmins: FacilityDetailUserItem[] = [];

  loading = true;
  loadingProtocol = false;
  facility: Facility;
  moveToProductionActive = false;

  running = 0;

  isUserAdmin: boolean;

  private mapAttributes() {
    this.facility.serviceAttrs().forEach((attr, _) => {
      this.facilityAttrsService.push(new DetailViewItem(attr));
    });

    this.facility.organizationAttrs().forEach((attr, _) => {
      this.facilityAttrsOrganization.push(new DetailViewItem(attr));
    });

    this.facility.protocolAttrs().forEach((attr, _) => {
      this.facilityAttrsProtocol.push(new DetailViewItem(attr));
    });

    this.facility.accessControlAttrs().forEach((attr, _) => {
      this.facilityAttrsAccessControl.push(new DetailViewItem(attr));
    });

    this.facilityAttrsService = FacilitiesDetailComponent.sortItems(this.facilityAttrsService);
    this.facilityAttrsOrganization = FacilitiesDetailComponent.sortItems(this.facilityAttrsOrganization);
    this.facilityAttrsProtocol = FacilitiesDetailComponent.sortItems(this.facilityAttrsProtocol);
    this.facilityAttrsAccessControl = FacilitiesDetailComponent.sortItems(this.facilityAttrsAccessControl);
  }

  private mapAdmins() {
    this.facilityAdmins = [];
    this.facility.managers.forEach(user => {
      this.facilityAdmins.push(new FacilityDetailUserItem(user));
    });
  }

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.isUserAdmin = AppComponent.isApplicationAdmin();
      this.facilitiesService.getFacility(params['id']).subscribe((data) => {
        this.facility = new Facility(data);

        this.mapAttributes();
        this.mapAdmins();

        if (this.facility.activeRequestId) {
          this.loadMoveToProductionActive(this.facility.activeRequestId);
        }
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });

    this.isUserAdmin = AppComponent.isApplicationAdmin();
  }

  loadMoveToProductionActive(activeRequestId: number) {
    this.requestsService.getRequest(activeRequestId).subscribe(request => {
      this.moveToProductionActive = (request.action === 'MOVE_TO_PRODUCTION');
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  moveToProduction(): void {
    this.router.navigateByUrl('auth/facilities/moveToProduction/' + this.facility.id );
  }

  openDeleteDialog(): void {
    this.dialog.open(FacilitiesDetailDeleteDialogComponent, {
      width: '50%',
      data: {parent: this, facilityName: this.facility.name}
    });
  }

  openClientSecretDialog(): void {
    this.dialog.open(FacilitiesDetailClientSecretDialogComponent, {
      width: '50%',
      data: {parent: this, facilityName: undefined}
    });
  }

  deleteFacility(): void {
    this.facilitiesService.removeFacility(this.facility.id).subscribe(id => {
      this.loading = false;
      this.router.navigateByUrl('auth/requests/detail/' + id);
    });
  }

  addFacilityAdmin(): void {
    this.router.navigateByUrl('auth/facilities/addAdmin/' + this.facility.id );
  }

  regenerateClientSecret(): void {
    this.loadingProtocol = true;
    this.facilitiesService.regenerateClientSecret(this.facility.id).subscribe(data => {
      const attr = new PerunAttribute(data)
      let index = -1;
      this.facilityAttrsProtocol.forEach(pair => {
        if (pair.urn === attr.fullName) {
          index = this.facilityAttrsProtocol.indexOf(pair);
        }
      });
      if (index != -1) {
        this.facilityAttrsProtocol[index] = new DetailViewItem(attr);
      }
      this.loadingProtocol = false;
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

  private static sortItems(items: DetailViewItem[]): DetailViewItem[] {
    items.sort((a, b) => {
      return a.position - b.position;
    });

    return items;
  }
}
