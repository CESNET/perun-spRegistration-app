import {Component,  Input} from '@angular/core';

@Component({
  selector: 'request-item-input-error',
  templateUrl: './request-item-input-error.component.html',
  styleUrls: ['./request-item-input-error.component.scss']
})
export class RequestItemInputErrorComponent {

  @Input() messageKey = '';

}
