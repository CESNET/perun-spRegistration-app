import { Component, Input } from '@angular/core';
import { IconDefinition } from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: 'app-menu-button',
  templateUrl: './menu-button.component.html',
  styleUrls: ['./menu-button.component.scss']
})
export class MenuButtonComponent {

  constructor() { }

  @Input()
  icon: IconDefinition;

  @Input()
  route: string;

  @Input()
  text: string;
}
