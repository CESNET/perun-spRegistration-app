import {Component, DoCheck, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {RequestsService} from '../../core/services/requests.service';
import {Subscription} from 'rxjs';
import {Request} from '../../core/models/Request';
import {NgModel} from '@angular/forms';
import {PerunAttribute} from '../../core/models/PerunAttribute';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {AppComponent} from '../../app.component';
import {RequestSignature} from '../../core/models/RequestSignature';
import {RequestDetailDialogComponent} from './request-detail-dialog/request-detail-dialog.component';
import {DetailViewItem} from '../../core/models/items/DetailViewItem';

export interface DialogData {
  isApprove: false;
  isSetWFC: false;
  isCancel: false;
  parent: RequestDetailComponent;
}

@Component({
  selector: 'app-request-detail',
  templateUrl: './request-detail.component.html',
  styleUrls: ['./request-detail.component.scss']
})
export class RequestDetailComponent implements OnInit, DoCheck, OnDestroy {

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router
  ) { }

  private sub: Subscription;
  requestAttrsService: DetailViewItem[] = [];
  requestAttrsOrganization: DetailViewItem[] = [];
  requestAttrsProtocol: DetailViewItem[] = [];
  requestAttrsAccessControl: DetailViewItem[] = [];

  serviceChangedCnt = Number(0);
  organizationChangedCnt = Number(0);
  protocolChangedCnt = Number(0);
  accessControlChangedCnt = Number(0);

  request: Request = null;
  signatures: RequestSignature[] = [];

  displayedColumns: string[] = ['name', 'value'];

  loading = true;
  expansionPanelDisabled = true;
  icon = true;

  @ViewChild('input', {static: false})
  inputField: NgModel;

  successApproveMessage: string;
  successRejectMessage: string;
  successSetWFCMessage: string;
  successCancelMessage: string;
  noCommentErrorMessage: string;

  isApplicationAdmin: boolean = false;
  filterChangedOnly = false;

  displayOldVal: boolean;
  includeComment: boolean;

  private static sortItems(items: DetailViewItem[]): DetailViewItem[] {
    items.sort((a, b) => {
      return a.position - b.position;
    });

    return items;
  }

  private mapAttributes() {
    const actionUpdate = this.request.action === 'UPDATE_FACILITY';
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

    this.requestAttrsService = RequestDetailComponent.sortItems(this.requestAttrsService);
    this.requestAttrsOrganization = RequestDetailComponent.sortItems(this.requestAttrsOrganization);
    this.requestAttrsProtocol = RequestDetailComponent.sortItems(this.requestAttrsProtocol);
    this.requestAttrsAccessControl = RequestDetailComponent.sortItems(this.requestAttrsAccessControl);
  }

  ngOnInit() {
    this.resetFields();
    this.sub = this.route.params.subscribe(params => {
      this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = new Request(request);
        this.mapAttributes();
        this.displayOldVal = request.action === 'UPDATE_FACILITY';
        this.includeComment = request.status !== 'APPROVED' && request.status !== 'REJECTED';
        this.requestsService.getSignatures(this.request.reqId).subscribe(signatures => {
          this.signatures = signatures.map(s => new RequestSignature(s));
          if (this.signatures.length !== 0) {
            this.expansionPanelDisabled = false;
          }
          this.loading = false;
        });
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
    this.sub.unsubscribe();
  }

  ngDoCheck(): void {
    if (this.request !== undefined && this.request !== null) {
      if (this.request.status !== 'APPROVED' && this.request.status !== 'REJECTED') {
        this.displayedColumns = ['name', 'value', 'comment'];
      } else {
        this.displayedColumns = ['name', 'value'];
      }
    }
  }

  openApproveDialog(): void {
    this.dialog.open(RequestDetailDialogComponent, {
      width: '400px',
      data: {isApprove: true, isSetWFC: false, isCancel: false, parent: this}
    });
  }

  openRejectDialog(): void {
    this.dialog.open(RequestDetailDialogComponent, {
      width: '400px',
      data: {isApprove: false, isSetWFC: false, isCancel: false, parent: this}
    });
  }

  openSetWFCDialog(): void {
    this.dialog.open(RequestDetailDialogComponent, {
      width: '400px',
      data: {isApprove: false, isSetWFC: true , isCancel: false, parent: this}
    });
  }

  openCancelDialog(): void {
    this.dialog.open(RequestDetailDialogComponent, {
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
      this.router.navigate(['/auth/requests/allRequests']).then();
    }, error => {
      console.log('Error');
      console.log(error);
    });

  }

  approve() {
    this.requestsService.approveRequest(this.request.reqId).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successApproveMessage, null, {duration: 6000});
      this.router.navigate(['/auth/requests/allRequests']).then();
    }, error => {
      console.log('Error');
      console.log(error);
    });
  }

  cancel() {
    this.requestsService.cancelRequest(this.request.reqId).subscribe(_ => {
      this.loading = false;
      this.snackBar.open(this.successCancelMessage, null, {duration: 6000});
      this.router.navigate(['/auth/requests/allRequests']).then();
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

  changeArrow() {
    this.icon = !this.icon;
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
}
