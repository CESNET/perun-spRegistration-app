import {Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ConfigService} from '../../core/services/config.service';
import {ApplicationItem} from '../../core/models/ApplicationItem';
import {RequestsService} from '../../core/services/requests.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Request} from '../../core/models/Request';
import {RequestItemInputComponent} from '../../shared/request-item-input/request-item-input.component';
import {UrnValuePair} from '../../core/models/UrnValuePair';
import {PerunAttribute} from '../../core/models/PerunAttribute';

@Component({
  selector: 'app-request-edit',
  templateUrl: './request-edit.component.html',
  styleUrls: ['./request-edit.component.scss']
})
export class RequestEditComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private configService: ConfigService,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router) { }

  @ViewChildren('commentedServiceItems') commentedServiceItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('serviceItems') serviceItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('commentedOrgItems') commentedOrgItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('orgItems') orgItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('commentedProtocolItems') commentedProtocolItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('protocolItems') protocolItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('commentedAccessItems') commentedAccessItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('accessItems') accessItems: QueryList<RequestItemInputComponent>;

  private sub: any;
  loading = true;
  isCardBodyVisible = false;
  isFormVisible = false;
  snackBarDurationMs = 5000;

  showServiceMore = false;
  showOrganizationMore = false;
  showProtocolMore = false;
  showAccessControlMore = false;

  request: Request;

  errorText: string;
  successActionText: string;

  commentedServiceAttrs: ApplicationItem[] = [];
  commentedOrganizationAttrs: ApplicationItem[] = [];
  commentedProtocolAttrs: ApplicationItem[] = [];
  commentedAccessControlAttrs: ApplicationItem[] = [];

  serviceAttrs: ApplicationItem[] = [];
  organizationAttrs: ApplicationItem[] = [];
  protocolAttrs: ApplicationItem[] = [];
  accessControlAttrs: ApplicationItem[] = [];

  private static filterAndSort(items: ApplicationItem[]): ApplicationItem[] {
    items = this.filterItems(items);
    items = this.sortItems(items);
    return items;
  }

  private static filterItems(items: ApplicationItem[]): ApplicationItem[] {
    items.filter((item) => item.displayed);
    return items;
  }

  private static sortItems(items: ApplicationItem[]): ApplicationItem[] {
    items.sort((a, b) => {
      return a.displayPosition - b.displayPosition;
    });

    return items;
  }

  private static pushInput(attr: PerunAttribute, commentedDest: ApplicationItem[], regularDest: ApplicationItem[]) {
    if (!attr.input.editable) {
      return;
    }
    attr.input.oldValue = attr.value;

    if (attr.comment) {
      attr.input.editable = true;
      attr.input.comment = attr.comment;
      commentedDest.push(attr.input);
    } else {
      regularDest.push(attr.input);
    }
  }

  private static pushAttr(i: RequestItemInputComponent, perunAttributes: PerunAttribute[]) {
    const attr = i.getAttribute();
    const perunAttr = new UrnValuePair(attr.value, attr.urn);
    perunAttributes.push(perunAttr);
  }

  ngOnInit() {
    this.clearArrays();
    this.translate.get('REQUESTS.ERRORS.VALUES_ERROR_MESSAGE')
      .subscribe(value => this.errorText = value);
    this.translate.get('REQUESTS.SUCCESSFULLY_SUBMITTED')
      .subscribe(value => this.successActionText = value);
    this.getAttributes();
  }

  revealForm() {
    this.loading = false;
    this.isCardBodyVisible = true;
    this.isFormVisible = true;
  }

  hasCorrectValues(): boolean {
    let res = true;
    this.serviceItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.commentedServiceItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.orgItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.commentedOrgItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.protocolItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.commentedProtocolItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.accessItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});
    this.commentedAccessItems.forEach(i => { if (!i.hasCorrectValue()) { res = false; }});

    return res;
  }

  submitRequest() {
    if (!this.hasCorrectValues()) {
      return;
    }

    this.loading = true;

    const perunAttributes: UrnValuePair[] = [];

    this.serviceItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.commentedServiceItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.orgItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.commentedOrgItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.protocolItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.commentedProtocolItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.accessItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });
    this.commentedAccessItems.forEach(i => { RequestEditComponent.pushAttr(i, perunAttributes); });

    this.requestsService.updateRequest(this.request.reqId, perunAttributes).subscribe(_ => {
      this.snackBar.open(this.successActionText, null, {duration: this.snackBarDurationMs});
      this.router.navigate(['/auth/requests/detail/' + this.request.reqId]);
    });
  }

  private clearArrays(): void {
    this.commentedServiceAttrs = [];
    this.commentedOrganizationAttrs = [];
    this.commentedProtocolAttrs = [];
    this.commentedAccessControlAttrs = [];

    this.serviceAttrs = [];
    this.organizationAttrs = [];
    this.protocolAttrs = [];
    this.accessControlAttrs = [];
  }

  private getAttributes(): void {
    this.sub = this.route.params.subscribe(params => {
      this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = new Request(request);
        this.pushInputs();
        this.filterAndSortArrays();
        this.revealForm();
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });
  }

  private filterAndSortArrays() {
    this.commentedServiceAttrs = RequestEditComponent.filterAndSort(this.commentedServiceAttrs);
    this.commentedOrganizationAttrs = RequestEditComponent.filterAndSort(this.commentedOrganizationAttrs);
    this.commentedProtocolAttrs = RequestEditComponent.filterAndSort(this.commentedProtocolAttrs);
    this.commentedAccessControlAttrs = RequestEditComponent.filterAndSort(this.commentedAccessControlAttrs);

    this.serviceAttrs = RequestEditComponent.filterAndSort(this.serviceAttrs);
    this.organizationAttrs = RequestEditComponent.filterAndSort(this.organizationAttrs);
    this.protocolAttrs = RequestEditComponent.filterAndSort(this.protocolAttrs);
    this.accessControlAttrs = RequestEditComponent.filterAndSort(this.accessControlAttrs);
  }

  private pushInputs() {
    this.clearArrays();
    this.request.serviceAttrs().forEach((attr, _) => {
      RequestEditComponent.pushInput(attr, this.commentedServiceAttrs, this.serviceAttrs);
    });

    this.request.organizationAttrs().forEach((attr, _) => {
      RequestEditComponent.pushInput(attr, this.commentedOrganizationAttrs, this.organizationAttrs);
    });

    this.request.protocolAttrs().forEach((attr, _) => {
      RequestEditComponent.pushInput(attr, this.commentedProtocolAttrs, this.protocolAttrs);
    });

    this.request.accessControlAttrs().forEach((attr, _) => {
      RequestEditComponent.pushInput(attr, this.commentedAccessControlAttrs, this.accessControlAttrs);
    });
  }

  changeServiceShowMore() {
    this.showServiceMore = !this.showServiceMore;
  }

  changeOrganizationShowMore() {
    this.showOrganizationMore = !this.showOrganizationMore;
  }

  changeProtocolShowMore() {
    this.showProtocolMore = !this.showProtocolMore;
  }

  changeAccessShowMore() {
    this.showAccessControlMore = !this.showAccessControlMore;
  }

}
