import {
  Component,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { ConfigService } from '../../core/services/config.service';
import { ApplicationItem } from '../../core/models/ApplicationItem';
import { RequestsService } from '../../core/services/requests.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepper } from '@angular/material/stepper';
import { TranslateService } from '@ngx-translate/core';
import { PerunAttribute } from '../../core/models/PerunAttribute';
import { RequestsRegisterServiceStepComponent } from './requests-register-service-step/requests-register-service-step.component';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-new-request',
  templateUrl: './requests-register-service.component.html',
  styleUrls: ['./requests-register-service.component.scss'],
})
export class RequestsRegisterServiceComponent implements OnInit, OnDestroy {
  constructor(
    private configService: ConfigService,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router
  ) {}

  @ViewChildren(RequestsRegisterServiceStepComponent)
  steps: QueryList<RequestsRegisterServiceStepComponent>;

  @ViewChild(MatStepper, { static: false }) stepper: MatStepper;

  enabledProtocolsSubscription: Subscription;
  errorMessageSubscription: Subscription;
  successMessageSubscription: Subscription;

  serviceSelected: string;

  formVisible = false;
  enabledProtocols: string[];
  loading = true;

  // translations
  errorText: string;
  successActionText: string;

  applicationItemGroups: ApplicationItem[][];

  /**
   * Filters items that should not be displayed
   *
   * @param items
   */
  private static filterItems(items: ApplicationItem[][]): ApplicationItem[][] {
    const filteredItems: ApplicationItem[][] = [];

    items.forEach(itemsGroup => {
      const filteredGroup: ApplicationItem[] = [];

      itemsGroup.forEach(item => {
        if (item.displayed) {
          filteredGroup.push(item);
        }
      });

      filteredItems.push(filteredGroup);
    });

    return filteredItems;
  }

  /**
   * Sorts items in order that they should be displayed
   *
   * @param items
   */
  private static sortItems(items: ApplicationItem[][]): ApplicationItem[][] {
    const sortedItems: ApplicationItem[][] = [];

    items.forEach(itemsGroup => {
      sortedItems.push(
        itemsGroup.sort((a, b) => a.displayPosition - b.displayPosition)
      );
    });

    return sortedItems;
  }

  ngOnInit() {
    this.enabledProtocolsSubscription = this.configService
      .getProtocolsEnabled()
      .subscribe(
        protocols => {
          this.enabledProtocols = [];
          if (protocols) {
            protocols.forEach(v => {
              if (v) {
                this.enabledProtocols.push(v.toLowerCase());
              }
            });
          }
          if (!this.enabledProtocols || this.enabledProtocols.length === 0) {
            console.log('Error, no protocols enabled');
          }
          if (this.enabledProtocols.indexOf('oidc') === -1) {
            this.samlSelected();
          } else if (this.enabledProtocols.indexOf('saml') === -1) {
            this.oidcSelected();
          }
          this.loading = false;
        },
        error => {
          this.loading = false;
          console.log(error);
        }
      );

    this.errorMessageSubscription = this.translate
      .get('REQUESTS.ERRORS.VALUES_ERROR_MESSAGE')
      .subscribe(value => (this.errorText = value));
    this.successMessageSubscription = this.translate
      .get('REQUESTS.SUCCESSFULLY_SUBMITTED')
      .subscribe(value => (this.successActionText = value));
  }

  ngOnDestroy(): void {
    if (this.enabledProtocolsSubscription) {
      this.enabledProtocolsSubscription.unsubscribe();
    }
    if (this.errorMessageSubscription) {
      this.errorMessageSubscription.unsubscribe();
    }
    if (this.successMessageSubscription) {
      this.successMessageSubscription.unsubscribe();
    }
  }

  revealForm() {
    this.loading = false;
    this.formVisible = true;
  }

  onLoading() {
    this.loading = true;
    this.formVisible = false;
  }

  oidcSelected() {
    this.onLoading();
    this.serviceSelected = 'oidc';

    this.configService.getOidcApplicationItems().subscribe(items => {
      items = items.map(category =>
        category.map(item => new ApplicationItem(item))
      );
      this.applicationItemGroups = RequestsRegisterServiceComponent.sortItems(
        RequestsRegisterServiceComponent.filterItems(items)
      );
      this.revealForm();
    });
  }

  samlSelected() {
    this.onLoading();
    this.serviceSelected = 'saml';

    this.configService.getSamlApplicationItems().subscribe(items => {
      items = items.map(category =>
        category.map(item => new ApplicationItem(item))
      );
      this.applicationItemGroups = RequestsRegisterServiceComponent.sortItems(
        RequestsRegisterServiceComponent.filterItems(items)
      );
      this.revealForm();
    });
  }

  /**
   * Collects data from form and submits new request
   */
  submitRequest() {
    this.loading = true;
    let perunAttributes: PerunAttribute[] = [];

    this.steps.forEach(
      step =>
        (perunAttributes = perunAttributes.concat(step.getPerunAttributes()))
    );

    this.requestsService.createRegistrationRequest(perunAttributes).subscribe(
      requestId => {
        this.loading = false;
        this.snackBar.open(this.successActionText, null, { duration: 6000 });
        this.router.navigate(['/auth/requests/detail/' + requestId]);
      },
      error => {
        this.loading = false;
        console.log(error);
      }
    );
  }

  previousStep() {
    this.stepper.previous();
  }
}
