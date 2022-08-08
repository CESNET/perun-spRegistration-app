import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ConfigService } from '../../core/services/config.service';
import { ApplicationItem } from '../../core/models/ApplicationItem';
import { RequestsService } from '../../core/services/requests.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RequestItemInputComponent } from '../../shared/request-item-input/request-item-input.component';
import { UrnValuePair } from '../../core/models/UrnValuePair';
import { PerunAttribute } from '../../core/models/PerunAttribute';
import { Facility } from '../../core/models/Facility';
import { AppComponent } from '../../app.component';
import { FacilitiesService } from '../../core/services/facilities.service';

@Component({
  selector: 'app-facilities-transfer',
  templateUrl: './facilities-transfer.component.html',
  styleUrls: ['./facilities-transfer.component.scss'],
})
export class FacilitiesTransferComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private configService: ConfigService,
    private requestsService: RequestsService,
    private facilitiesService: FacilitiesService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router
  ) {}

  @ViewChildren('commentedServiceItems')
  commentedServiceItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('serviceItems')
  serviceItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('commentedOrgItems')
  commentedOrgItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('orgItems') orgItems: QueryList<RequestItemInputComponent>;
  @ViewChildren('commentedProtocolItems')
  commentedProtocolItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('protocolItems')
  protocolItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('commentedAccessItems')
  commentedAccessItems: QueryList<RequestItemInputComponent>;

  @ViewChildren('accessItems')
  accessItems: QueryList<RequestItemInputComponent>;

  private sub: any;
  loading = true;
  isCardBodyVisible = false;
  isFormVisible = false;
  snackBarDurationMs = 5000;

  showServiceMore = false;
  showOrganizationMore = false;
  showProtocolMore = false;
  showAccessControlMore = false;

  errorText: string;
  successActionText: string;
  errorWronglyFilledItem: string;

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
    return items.filter(item => item.displayed);
  }

  private static sortItems(items: ApplicationItem[]): ApplicationItem[] {
    return items.sort((a, b) => a.displayPosition - b.displayPosition);
  }

  private static pushInput(
    attr: PerunAttribute,
    regularDest: ApplicationItem[]
  ) {
    if (!attr.input.editable) {
      return;
    }
    attr.input.oldValue = attr.value;
    regularDest.push(attr.input);
  }

  private static pushAttr(
    i: RequestItemInputComponent,
    perunAttributes: PerunAttribute[]
  ) {
    const attr = i.getAttribute();
    const perunAttr = new UrnValuePair(attr.value, attr.urn);
    perunAttributes.push(perunAttr);
  }

  isUserAdmin: boolean;
  facility: Facility;

  ngOnInit() {
    this.clearArrays();
    this.translate
      .get('REQUESTS.ERRORS.VALUES_ERROR_MESSAGE')
      .subscribe(value => (this.errorText = value));
    this.translate
      .get('REQUESTS.SUCCESSFULLY_SUBMITTED')
      .subscribe(value => (this.successActionText = value));
    this.sub = this.route.params.subscribe(params => {
      this.isUserAdmin = AppComponent.isApplicationAdmin();
      this.facilitiesService.getFacility(params.id).subscribe(
        data => {
          this.facility = new Facility(data);
          this.pushInputs();
          this.filterAndSortArrays();
          this.revealForm();
        },
        error => {
          this.loading = false;
          console.log(error);
        }
      );
    });
    this.translate
      .get('FACILITIES.WRONGLY_FILLED_ITEM')
      .subscribe(value => (this.errorWronglyFilledItem = value));
  }

  revealForm() {
    this.loading = false;
    this.isCardBodyVisible = true;
    this.isFormVisible = true;
  }

  private validate(
    i: RequestItemInputComponent,
    perunAttributes: PerunAttribute[]
  ): boolean {
    const attr = i.getAttribute();
    const perunAttr = new UrnValuePair(attr.value, attr.urn);
    if (!i.hasCorrectValue()) {
      this.snackBar.open(this.errorWronglyFilledItem, null, { duration: 6000 });
      this.loading = false;
      return false;
    }
    perunAttributes.push(perunAttr);
    return true;
  }

  submitRequest() {
    this.loading = true;
    const perunAttributes: UrnValuePair[] = [];

    // set to false when one attribute has wrong value
    let allGood = true;
    this.serviceItems.forEach(i => {
      if (!this.validate(i, perunAttributes)) {
        allGood = false;
        this.showServiceMore = true;
      }
    });
    this.orgItems.forEach(i => {
      if (!this.validate(i, perunAttributes)) {
        allGood = false;
        this.showOrganizationMore = true;
      }
    });
    this.protocolItems.forEach(i => {
      if (!this.validate(i, perunAttributes)) {
        allGood = false;
        this.showProtocolMore = true;
      }
    });
    this.accessItems.forEach(i => {
      if (!this.validate(i, perunAttributes)) {
        allGood = false;
        this.showAccessControlMore = true;
      }
    });

    if (!allGood) {
      return;
    }

    this.requestsService
      .createTransferRequest(this.facility.id, perunAttributes)
      .subscribe(reqId => {
        this.snackBar.open(this.successActionText, null, {
          duration: this.snackBarDurationMs,
        });
        this.router.navigate(['/auth/requests/detail/' + reqId]);
      });
  }

  private clearArrays(): void {
    this.serviceAttrs = [];
    this.organizationAttrs = [];
    this.protocolAttrs = [];
    this.accessControlAttrs = [];
  }

  private filterAndSortArrays() {
    this.serviceAttrs = FacilitiesTransferComponent.filterAndSort(
      this.serviceAttrs
    );
    this.organizationAttrs = FacilitiesTransferComponent.filterAndSort(
      this.organizationAttrs
    );
    this.protocolAttrs = FacilitiesTransferComponent.filterAndSort(
      this.protocolAttrs
    );
    this.accessControlAttrs = FacilitiesTransferComponent.filterAndSort(
      this.accessControlAttrs
    );
  }

  private pushInputs() {
    this.clearArrays();
    this.facility.serviceAttrs().forEach((attr, _) => {
      FacilitiesTransferComponent.pushInput(attr, this.serviceAttrs);
    });

    this.facility.organizationAttrs().forEach((attr, _) => {
      FacilitiesTransferComponent.pushInput(attr, this.organizationAttrs);
    });

    this.facility.protocolAttrs().forEach((attr, _) => {
      FacilitiesTransferComponent.pushInput(attr, this.protocolAttrs);
    });

    this.facility.accessControlAttrs().forEach((attr, _) => {
      FacilitiesTransferComponent.pushInput(attr, this.accessControlAttrs);
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
