import { Component, OnInit } from '@angular/core';
import {AppComponent} from '../../app.component';

@Component({
  selector: 'app-not-found-page',
  templateUrl: './not-found-page.component.html',
  styleUrls: ['./not-found-page.component.scss']
})
export class NotFoundPageComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

  hasUser(): boolean {
    return AppComponent.hasUser();
  }

}
