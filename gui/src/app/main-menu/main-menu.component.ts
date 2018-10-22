import {Component} from '@angular/core';
import {faDatabase, faEnvelopeOpen, faPlus} from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: 'app-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent {

  requestsIcon = faEnvelopeOpen;
  facilitiesIcon = faDatabase;
  newRequestIcon = faPlus;

  constructor() { }

}
