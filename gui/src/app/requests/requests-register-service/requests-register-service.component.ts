import {
  Component,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core'
import { ConfigService } from '../../core/services/config.service'
import { ApplicationItem } from '../../core/models/ApplicationItem'
import { RequestsService } from '../../core/services/requests.service'
import { MatSnackBar } from '@angular/material/snack-bar'
import { MatStepper } from '@angular/material/stepper'
import { TranslateService } from '@ngx-translate/core'
import { PerunAttribute } from '../../core/models/PerunAttribute'
import { RequestsRegisterServiceStepComponent } from './requests-register-service-step/requests-register-service-step.component'
import { Router } from '@angular/router'

@Component({
  selector: 'app-new-request',
  templateUrl: './requests-register-service.component.html',
  styleUrls: ['./requests-register-service.component.scss']
})
export class RequestsRegisterServiceComponent implements OnInit {
  constructor (
    private configService: ConfigService,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router
  ) {}

  @ViewChildren(RequestsRegisterServiceStepComponent)
    steps: QueryList<RequestsRegisterServiceStepComponent>

  @ViewChild(MatStepper, { static: false })
    stepper: MatStepper

  serviceSelected: string

  isFormVisible = false
  isCardBodyVisible = false
  enabledProtocols: string[]
  loading = true
  selected = ''

  // translations
  errorText: string
  successActionText: string

  applicationItemGroups: ApplicationItem[][]

  /**
   * Filters items that should not be displayed
   *
   * @param items
   */
  private static filterItems (items: ApplicationItem[][]): ApplicationItem[][] {
    const filteredItems: ApplicationItem[][] = []

    items.forEach((itemsGroup) => {
      const filteredGroup: ApplicationItem[] = []

      itemsGroup.forEach((item) => {
        if (item.displayed) {
          filteredGroup.push(item)
        }
      })

      filteredItems.push(filteredGroup)
    })

    return filteredItems
  }

  /**
   * Sorts items in order that they should be displayed
   *
   * @param items
   */
  private static sortItems (items: ApplicationItem[][]): ApplicationItem[][] {
    const sortedItems: ApplicationItem[][] = []

    items.forEach((itemsGroup) => {
      sortedItems.push(
        itemsGroup.sort((a, b) => a.displayPosition - b.displayPosition)
      )
    })

    return sortedItems
  }

  ngOnInit () {
    this.configService.getProtocolsEnabled().subscribe(
      (protocols) => {
        this.enabledProtocols = protocols
        this.loading = false
        this.enabledProtocols.forEach((v) => v.toLowerCase())
        if (this.enabledProtocols.indexOf('oidc') === -1) {
          this.samlSelected()
        } else if (this.enabledProtocols.indexOf('saml') === -1) {
          this.oidcSelected()
        }
      },
      (error) => {
        this.loading = false
        console.log(error)
      }
    )

    this.translate
      .get('REQUESTS.ERRORS.VALUES_ERROR_MESSAGE')
      .subscribe((value) => (this.errorText = value))
    this.translate
      .get('REQUESTS.SUCCESSFULLY_SUBMITTED')
      .subscribe((value) => (this.successActionText = value))
  }

  revealForm () {
    this.loading = false
    this.isCardBodyVisible = true
    this.isFormVisible = true
  }

  onLoading () {
    this.loading = true
    this.isCardBodyVisible = false
  }

  oidcSelected () {
    this.onLoading()
    this.selected = 'oidc'

    this.configService.getOidcApplicationItems().subscribe((items) => {
      items = items.map((category) =>
        category.map((item) => new ApplicationItem(item))
      )
      this.applicationItemGroups = RequestsRegisterServiceComponent.sortItems(
        RequestsRegisterServiceComponent.filterItems(items)
      )
      this.revealForm()
    })
  }

  samlSelected () {
    this.onLoading()
    this.selected = 'saml'

    this.configService.getSamlApplicationItems().subscribe((items) => {
      items = items.map((category) =>
        category.map((item) => new ApplicationItem(item))
      )
      this.applicationItemGroups = RequestsRegisterServiceComponent.sortItems(
        RequestsRegisterServiceComponent.filterItems(items)
      )
      this.revealForm()
    })
  }

  /**
   * Collects data from form and submits new request
   */
  submitRequest () {
    this.loading = true
    let perunAttributes: PerunAttribute[] = []

    this.steps.forEach(
      (step) =>
        (perunAttributes = perunAttributes.concat(step.getPerunAttributes()))
    )

    this.requestsService.createRegistrationRequest(perunAttributes).subscribe(
      (requestId) => {
        this.loading = false
        this.snackBar.open(this.successActionText, null, { duration: 6000 })
        this.router.navigate(['/auth/requests/detail/' + requestId])
      },
      (error) => {
        this.loading = false
        console.log(error)
      }
    )
  }

  previousStep () {
    this.stepper.previous()
  }
}
