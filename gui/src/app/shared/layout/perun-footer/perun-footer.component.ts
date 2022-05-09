import { Component, OnInit } from '@angular/core'
import { Subscription } from 'rxjs'
import { ConfigService } from '../../../core/services/config.service'

@Component({
  selector: 'app-perun-footer',
  template: '<div [innerHTML]="footerHTML"></div>',
  styleUrls: ['./perun-footer.component.scss']
})
export class PerunFooterComponent {
  constructor (private configService: ConfigService) {}

  footerHTML: string

}
