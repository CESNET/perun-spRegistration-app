import {Component, DoCheck, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {RequestsService} from '../../core/services/requests.service';
import {Subscription} from 'rxjs';
import {Request} from '../../core/models/Request';
import {NgModel} from '@angular/forms';
import {PerunAttribute} from '../../core/models/PerunAttribute';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {AppComponent} from '../../app.component';
import {RequestSignature} from '../../core/models/RequestSignature';
import {RequestsDetailDialogComponent} from './requests-detail-dialog/requests-detail-dialog.component';
import {DetailViewItem} from '../../core/models/items/DetailViewItem';
import {AuditService} from "../../core/services/audit.service";
import {AuditLog} from "../../core/models/AuditLog";
import {RequestAction} from "../../core/models/enums/RequestAction";
import {RequestStatus} from "../../core/models/enums/RequestStatus";

export interface DialogData {
  isApprove: false;
  isSetWFC: false;
  isCancel: false;
  parent: RequestsDetailComponent;
}

@Component({
  selector: 'app-request-detail',
  templateUrl: './requests-detail.component.html',
  styleUrls: ['./requests-detail.component.scss']
})
export class RequestsDetailComponent implements OnInit, DoCheck, OnDestroy {

  private routeSubscription: Subscription = null;
  private requestSubscription: Subscription = null;
  private signaturesSubscription: Subscription = null;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private auditService: AuditService
  ) { }


  @ViewChild('input', {static: false})
  inputField: NgModel;

  requestActionEnum = RequestAction;
  requestStatusEnum = RequestStatus;

  requestAttrsService: DetailViewItem[] = [];
  requestAttrsOrganization: DetailViewItem[] = [];
  requestAttrsProtocol: DetailViewItem[] = [];
  requestAttrsAccessControl: DetailViewItem[] = [];

  serviceChangedCnt: number = 0;
  organizationChangedCnt: number = 0
  protocolChangedCnt: number = 0
  accessControlChangedCnt: number = 0;

  request: Request = null;
  signatures: RequestSignature[] = [];
  audits: AuditLog[] = [];

  displayedColumns: string[] = ['name', 'value'];

  loading: boolean = true;
  auditLoading: boolean = false;
  signaturesLoading: boolean = false;
  signaturesExpansionPanelDisabled: boolean = true;

  successApproveMessage: string = '';
  successRejectMessage: string = '';
  successSetWFCMessage: string = '';
  successCancelMessage: string = '';
  noCommentErrorMessage: string = '';

  isApplicationAdmin: boolean = false;
  filterChangedOnly: boolean = false;

  displayOldVal: boolean = false;
  includeComment: boolean = false;

  private static sortItems(items: DetailViewItem[]): DetailViewItem[] {
    items.sort((a, b) => {
      return a.position - b.position;
    });

    return items;
  }

  ngOnInit() {
    this.resetFields();
    this.routeSubscription = this.route.params.subscribe(params => {
       this.requestSubscription = this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = new Request(request);
        if (this.request.action === RequestAction.MOVE_TO_PRODUCTION) {
          this.loadSignatures(this.request.reqId);
        }
        this.loadAudit(this.request.reqId);
        this.mapAttributes();
        this.displayOldVal = request.action === RequestAction.UPDATE_FACILITY;
        this.includeComment = request.status !== RequestStatus.APPROVED && request.status !== RequestStatus.REJECTED
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });
    this.translate.get('COMMON.ERROR').subscribe(value => this.noCommentErrorMessage = value);
    this.translate.get('REQUESTS.REJECTED').subscribe(value => this.successRejectMessage = value);
    this.translate.get('REQUESTS.APPROVED').subscribe(value => this.successApproveMessage = value);
    this.translate.get('REQUESTS.SET_WFC_DONE').subscribe(value => this.successSetWFCMessage = value);
    this.translate.get('REQUESTS.CANCELED').subscribe(value => this.successCancelMessage = value);
    this.isApplicationAdmin = AppComponent.isApplicationAdmin();
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.requestSubscription) {
      this.requestSubscription.unsubscribe();
    }
    if (this.signaturesSubscription) {
      this.signaturesSubscription.unsubscribe();
    }
  }

  ngDoCheck(): void {
    if (this.request !== undefined && this.request !== null) {
      if (this.request.status !== RequestStatus.APPROVED && RequestStatus.REJECTED) {
        this.displayedColumns = ['name', 'value', 'comment'];
      } else {
        this.displayedColumns = ['name', 'value'];
      }
    }
  }

  openApproveDialog(): void {
    this.dialog.open(RequestsDetailDialogComponent, {
      width: '400px',
      data: {isApprove: true, isSetWFC: false, isCancel: false, parent: this}
    });
  }

  openRejectDialog(): void {
    this.dialog.open(RequestsDetailDialogComponent, {
      width: '400px',
      data: {isApprove: false, isSetWFC: false, isCancel: false, parent: this}
    });
  }

  openSetWFCDialog(): void {
    this.dialog.open(RequestsDetailDialogComponent, {
      width: '400px',
      data: {isApprove: false, isSetWFC: true , isCancel: false, parent: this}
    });
  }

  openCancelDialog(): void {
    this.dialog.open(RequestsDetailDialogComponent, {
      width: '400px',
      data: {isApprove: false, isSetWFC: false, isCancel: true, parent: this}
    });
  }

  onLoading(): void {
    this.loading = true;
  }

  reject() {
    this.requestsService.rejectRequest(this.request.reqId).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successRejectMessage, null, {duration: 6000});
      this.ngOnInit();
    }, error => {
      console.log('Error');
      console.log(error);
    });

  }

  approve() {
    this.requestsService.approveRequest(this.request.reqId).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successApproveMessage, null, {duration: 6000});
      this.ngOnInit();
    }, error => {
      console.log('Error');
      console.log(error);
    });
  }

  cancel() {
    this.requestsService.cancelRequest(this.request.reqId).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successCancelMessage, null, {duration: 6000});
      this.ngOnInit();
    }, error => {
      console.log('Error');
      console.log(error);
    });
  }

  requestChanges() {
    this.fillComments();
    const array = this.generateCommentedItems();

    this.requestsService.askForChanges(this.request.reqId, array).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successSetWFCMessage, null, {duration: 6000});
      this.ngOnInit();
    }, error => {
      console.log('Error');
      console.log(error);
    });
  }

  fillComments(): void {
    this.requestAttrsService.forEach(item => {
      this.request.serviceAttrs().get(item.urn).comment = item.comment;
    });
    this.requestAttrsOrganization.forEach(item => {
      this.request.organizationAttrs().get(item.urn).comment = item.comment;
    });
    this.requestAttrsProtocol.forEach(item => {
      this.request.protocolAttrs().get(item.urn).comment = item.comment;
    });
    this.requestAttrsAccessControl.forEach(item => {
      this.request.accessControlAttrs().get(item.urn).comment = item.comment;
    });
  }

  generateCommentedItems(): Array<PerunAttribute> {
    const array: Array<PerunAttribute> = [];
    Array.from(this.request.serviceAttrs().values()).forEach(attr => {
      if (attr.comment && attr.comment.trim()) {
        array.push(attr);
      }
    });
    Array.from(this.request.organizationAttrs().values()).forEach(attr => {
      if (attr.comment && attr.comment.trim()) {
        array.push(attr);
      }
    });
    Array.from(this.request.protocolAttrs().values()).forEach(attr => {
      if (attr.comment && attr.comment.trim()) {
        array.push(attr);
      }
    });
    Array.from(this.request.accessControlAttrs().values()).forEach(attr => {
      if (attr.comment && attr.comment.trim()) {
        array.push(attr);
      }
    });

    return array;
  }

  isUndefined(value: any) {
    // TODO: extract to one common method, also used in facility-detail
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

  getBadge(counter: number) {
    return (counter && counter > 0) ? counter.toString() : '';
  }

  private resetFields() {
    this.requestAttrsService = [];
    this.requestAttrsOrganization = [];
    this.requestAttrsProtocol = [];
    this.requestAttrsAccessControl = [];

    this.serviceChangedCnt = Number(0);
    this.organizationChangedCnt = Number(0);
    this.protocolChangedCnt = Number(0);
    this.accessControlChangedCnt = Number(0);

    this.request = undefined;
    this.signatures = [];
  }

  private loadAudit(id: number) {
    this.auditLoading = true;
    this.auditService.getAuditsForRequest(id).subscribe(audits => {
      this.audits = audits.map(a => new AuditLog(a));
      this.auditLoading = false;
    }, error => {
      this.auditLoading = false;
      console.log(error);
    });
  }

  private mapAttributes() {
    const actionUpdate = this.request.action === RequestAction.UPDATE_FACILITY;
    this.request.serviceAttrs().forEach((attr, _) => {
      const item = new DetailViewItem(attr);
      if (actionUpdate && item.hasValueChanged()) {
        this.serviceChangedCnt++;
      }
      this.requestAttrsService.push(item);
    });
    this.request.organizationAttrs().forEach((attr, _) => {
      const item = new DetailViewItem(attr);
      if (actionUpdate && item.hasValueChanged()) {
        this.organizationChangedCnt++;
      }
      this.requestAttrsOrganization.push(item);
    });
    this.request.protocolAttrs().forEach((attr, _) => {
      const item = new DetailViewItem(attr);
      if (actionUpdate && item.hasValueChanged()) {
        this.protocolChangedCnt++;
      }
      this.requestAttrsProtocol.push(item);
    });
    this.request.accessControlAttrs().forEach((attr, _) => {
      const item = new DetailViewItem(attr);
      if (actionUpdate && item.hasValueChanged()) {
        this.accessControlChangedCnt++;
      }
      this.requestAttrsAccessControl.push(item);
    });

    this.requestAttrsService = RequestsDetailComponent.sortItems(this.requestAttrsService);
    this.requestAttrsOrganization = RequestsDetailComponent.sortItems(this.requestAttrsOrganization);
    this.requestAttrsProtocol = RequestsDetailComponent.sortItems(this.requestAttrsProtocol);
    this.requestAttrsAccessControl = RequestsDetailComponent.sortItems(this.requestAttrsAccessControl);
  }

  private loadSignatures(id: number) {
    this.signaturesLoading = true;
    this.signaturesSubscription = this.requestsService.getSignatures(id).subscribe(signatures => {
      this.signatures = signatures.map(s => new RequestSignature(s));
      if (this.signatures.length !== 0) {
        this.signaturesExpansionPanelDisabled = false;
      }
      this.signaturesLoading = false;
    }, error => {
      console.log(error);
      this.signaturesLoading = false;
    });
  }
}
