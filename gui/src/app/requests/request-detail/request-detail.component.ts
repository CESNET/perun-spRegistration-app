import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {RequestsService} from "../../core/services/requests.service";
import {Subscription} from "rxjs";
import {Request} from "../../core/models/Request";
import {NgModel} from "@angular/forms";
import {PerunAttribute} from "../../core/models/PerunAttribute";
import {MatDialog, MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {AppComponent} from "../../app.component";
import {RequestSignature} from "../../core/models/RequestSignature";
import {RequestDetailDialogComponent} from "./request-detail-dialog/request-detail-dialog.component";

export interface DialogData {
  isApprove: boolean;
  parent: RequestDetailComponent,
}

@Component({
    selector: 'app-request-detail',
    templateUrl: './request-detail.component.html',
    styleUrls: ['./request-detail.component.scss']
})
export class RequestDetailComponent implements OnInit {

    constructor(
        public dialog: MatDialog,
        private route: ActivatedRoute,
        private requestsService: RequestsService,
        private snackBar: MatSnackBar,
        private translate: TranslateService,
    ) {

    }

    private sub: Subscription;
    //TODO edit datatype
    requestItems: any[];
    displayedColumns: string[] = ['name', 'value', 'comment'];

    loading = true;
    request: Request;
    signatures: RequestSignature[];
    columns: string[] = ['name', 'signedAt'];
    expansionPanelDisabled: boolean = true;
    icon: boolean = true;

    @ViewChild('input')
    inputField: NgModel;

    successApproveMessage: string;
    successRejectMessage: string;
    successSetWFCMessage: string;
    noCommentErrorMessage: string;

    isUserAdmin: boolean;

    private mapAttributes() {
        this.requestItems = [];
        for (let urn of Object.keys(this.request.attributes)) {
            let item = this.request.attributes[urn];
            this.requestItems.push({
                "urn": urn,
                "value": item.value,
                "oldValue": item.oldValue,
                "name": item.definition.displayName,
                "comment": item.comment
            });
        }
    }

    ngOnInit() {
        this.sub = this.route.params.subscribe(params => {
            this.requestsService.getRequest(params['id']).subscribe(request => {
                this.request = request;
                this.mapAttributes();
                this.requestsService.getSignatures(this.request.reqId).subscribe(signatures => {
                  this.signatures = signatures;
                  if(this.signatures.length != 0){
                    this.expansionPanelDisabled = false;
                  }
                  this.loading = false;
                });
            }, error => {
                this.loading = false;
                console.log(error);
            });
        });
        this.translate.get("REQUEST.REQUEST_ERROR").subscribe(value => this.noCommentErrorMessage = value);
        this.translate.get("REQUEST.REJECTED").subscribe(value => this.successRejectMessage = value);
        this.translate.get("REQUEST.APPROVED").subscribe(value => this.successApproveMessage = value);
        this.translate.get("REQUEST.SET_WFC").subscribe(value => this.successSetWFCMessage = value);
        this.isUserAdmin = AppComponent.isUserAdmin();
    }

    ngOnDestroy(): void {
        this.sub.unsubscribe();
    }

    openApproveDialog(): void {
      const dialogRef = this.dialog.open(RequestDetailDialogComponent, {
        width: '250px',
        data: {isApprove: true, parent: this}
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed');
      });
    }

    openRejectDialog(): void {
      const dialogRef = this.dialog.open(RequestDetailDialogComponent, {
        width: '250px',
        data: {isApprove: false, parent: this}
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed');
      });
    }

    reject() {
      this.requestsService.rejectRequest(this.request.reqId).subscribe(bool => {
        this.snackBar.open(this.successRejectMessage, null, {duration: 6000});
        this.ngOnInit()
        }, error => {
        console.log("Error");
        console.log(error);
      });

    }

    approve() {
      this.requestsService.approveRequest(this.request.reqId).subscribe(bool => {
        this.snackBar.open(this.successApproveMessage, null, {duration: 6000});
        this.ngOnInit()
        }, error => {
        console.log("Error");
        console.log(error);
      });
    }

    requestChanges() {
      for (let item of this.requestItems) {
        this.request.attributes[item.urn].comment = item.comment;
      }
      let array: Array<PerunAttribute> = [];
      for (const [key, value] of Object.entries(this.request.attributes)) {
        if ((value.comment != "") && (value.comment != null)) {
          array.push(value)
        }
      }
      this.requestsService.askForChanges(this.request.reqId, array).subscribe(bool => {
        this.snackBar.open(this.successSetWFCMessage, null, {duration: 6000});
      }, error => {
        console.log("Error");
        console.log(error);
      });
    }

  changeArrow(){
    this.icon = !this.icon;
  }
}
