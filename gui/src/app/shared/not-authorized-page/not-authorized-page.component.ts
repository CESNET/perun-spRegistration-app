import { Component, OnInit } from '@angular/core';
import { AppComponent } from '../../app.component';

@Component({
  selector: 'app-not-authorized-page',
  templateUrl: './not-authorized-page.component.html',
  styleUrls: ['./not-authorized-page.component.scss'],
})
export class NotAuthorizedPageComponent {
  constructor() {}

  public hasUser(): boolean {
    return AppComponent.hasUser();
  }
}
