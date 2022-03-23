import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FacilitiesService} from '../../core/services/facilities.service';
import {Facility} from '../../core/models/Facility';
import {AppComponent} from '../../app.component';
import {MatDialog} from '@angular/material/dialog';
import {FacilitiesDetailDeleteDialogComponent} from './facilities-detail-delete-dialog/facilities-detail-delete-dialog.component';
import {RequestsService} from '../../core/services/requests.service';
import {Subscription} from 'rxjs';
import {PerunAttribute} from "../../core/models/PerunAttribute";
import {FacilityDetailUserItem} from "../../core/models/items/FacilityDetailUserItem";
import {DetailViewItem} from "../../core/models/items/DetailViewItem";
import {FacilitiesDetailClientSecretDialogComponent} from "./facilities-detail-client-secret-dialog/facilities-detail-client-secret-dialog.component";
import {FacilitiesRemoveAdminDialogComponent} from "./facilities-remove-admin-dialog/facilities-remove-admin-dialog.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {TranslateService} from "@ngx-translate/core";
import {AuditLog} from "../../core/models/AuditLog";
import {AuditService} from "../../core/services/audit.service";
import {RequestAction} from "../../core/models/enums/RequestAction";

export interface DialogData {
  parent: FacilitiesDetailComponent;
  facilityName: Map<string, string>;
}

export interface DialogData2 {
  parent: FacilitiesDetailComponent
  userName: Map<string, string>;
  userId: number;
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
    private auditService: AuditService,
    private router: Router,
    public dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) { }

  private sub: Subscription;

  facilityAttrsService: DetailViewItem[] = [];
  facilityAttrsOrganization: DetailViewItem[] = [];
  facilityAttrsProtocol: DetailViewItem[] = [];
  facilityAttrsAccessControl: DetailViewItem[] = [];
  facilityAdmins: FacilityDetailUserItem[] = [];

  audits: Map<number, AuditLog[]> = new Map<number, AuditLog[]>();

  loading = true;
  protocolLoading = false;
  auditLoading = false;

  facility: Facility;
  moveToProductionActive = false;

  isUserAdmin: boolean;

  adminRemoveSuccessText: string;
  adminRemoveFailText: string;
  snackBarDurationMs = 5000;

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
        this.loadAudit(this.facility.id);
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });

    this.isUserAdmin = AppComponent.isApplicationAdmin();
    this.translate.get('FACILITIES.DETAIL.ADMIN_NOT_REMOVED')
      .subscribe(value => this.adminRemoveFailText = value);
    this.translate.get('FACILITIES.DETAIL.ADMIN_REMOVED')
      .subscribe(value => this.adminRemoveSuccessText = value);
  }

  loadMoveToProductionActive(activeRequestId: number) {
    this.requestsService.getRequest(activeRequestId).subscribe(request => {
      this.moveToProductionActive = (request.action === RequestAction.MOVE_TO_PRODUCTION);
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

  openRemoveAdminDialog(userName: string, userId: number): void {
    this.dialog.open(FacilitiesRemoveAdminDialogComponent, {
      width: '50%',
      data: {parent: this, userName: userName, userId: userId}
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

  removeFacilityAdmin(adminToRemoveId: number): void {
    this.facilitiesService.removeAdmin(this.facility.id, adminToRemoveId).subscribe(result => {
      this.loading = false;
      if (result) {
        this.snackBar.open(this.adminRemoveSuccessText, null, {duration: this.snackBarDurationMs});
        if (AppComponent.getUser().id === adminToRemoveId) {
          this.router.navigateByUrl('auth');
        } else {
          this.ngOnInit();
        }
      } else {
        this.snackBar.open(this.adminRemoveFailText, null, {duration: this.snackBarDurationMs});
      }
    });
  }

  regenerateClientSecret(): void {
    this.protocolLoading = true;
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
      this.protocolLoading = false;
    }, error => {
      this.protocolLoading = false;
      console.log(error);
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

  private loadAudit(id: number) {
    this.auditLoading = true;
    this.auditService.getAuditsForService(id).subscribe(audits => {
      const mappedAudits = audits.map(a => new AuditLog(a));
      const map = new Map<number, AuditLog[]>();
      mappedAudits.forEach(auditLog => {
        if (!map.get(auditLog.requestId)) {
          map.set(auditLog.requestId, []);
        }
        map.get(auditLog.requestId).push(auditLog)
      })
      this.audits = map;
      this.auditLoading = false;
    }, error => {
      this.auditLoading = false;
      console.log(error);
    });
  }

}
