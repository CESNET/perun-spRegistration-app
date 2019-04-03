import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {RequestsService} from "../../core/services/requests.service";
import {Subscription} from "rxjs";
import {Request} from "../../core/models/Request";
import {NgModel} from "@angular/forms";
import {PerunAttribute} from "../../core/models/PerunAttribute";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {ConfigService} from "../../core/services/config.service";
import {AppComponent} from "../../app.component";
import {RequestSignature} from "../../core/models/RequestSignature";

@Component({
    selector: 'app-request-detail',
    templateUrl: './request-detail.component.html',
    styleUrls: ['./request-detail.component.scss']
})
export class RequestDetailComponent implements OnInit {

    constructor(
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
    sign: string;

    @ViewChild('input')
    inputField: NgModel;

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

    getSignatures(){
      this.requestsService.getSignatures(this.request.reqId).subscribe(signatures => {
        this.sign = "";
        for(let sign of signatures){
          this.sign += sign.name + " (" + new Date(sign.signedAt).toLocaleString() + "), ";
        }
        if(this.sign != ""){
          this.sign = this.sign.slice(0, this.sign.length - 2);
        }
      });
    }

    ngOnInit() {
        this.sub = this.route.params.subscribe(params => {
            this.requestsService.getRequest(params['id']).subscribe(request => {
                this.request = request;
                this.loading = false;
                this.mapAttributes();
                this.getSignatures();
            }, error => {
                this.loading = false;
                console.log(error);
            });
        });
        //TODO add translation
        this.translate.get("REQUEST.REQUEST_ERROR").subscribe(value => this.noCommentErrorMessage = value);
        this.isUserAdmin = AppComponent.isUserAdmin();
        //this.configService.isUserAdmin().subscribe(bool => this.isUserAdmin = bool);
    }

    ngOnDestroy(): void {
        this.sub.unsubscribe();
    }

    reject() {
        if (this.isUserAdmin) {
            this.requestsService.rejectRequest(this.request.reqId).subscribe(bool => {
                this.snackBar.open(this.noCommentErrorMessage, null, {duration: 6000});
            }, error => {
                console.log("Error");
                console.log(error);
            });
        }
    }

    approve() {
        if (this.isUserAdmin) {
            this.requestsService.approveRequest(this.request.reqId).subscribe(bool => {
                this.snackBar.open(this.noCommentErrorMessage, null, {duration: 6000});
            }, error => {
                console.log("Error");
                console.log(error);
            });
        }
    }

    requestChanges() {
        if (this.isUserAdmin) {
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
                this.snackBar.open(this.noCommentErrorMessage, null, {duration: 6000});
            }, error => {
                console.log("Error");
                console.log(error);
            });
        }
    }
}
