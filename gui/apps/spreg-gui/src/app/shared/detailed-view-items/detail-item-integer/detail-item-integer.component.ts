import {Component, Input, ViewChild} from '@angular/core';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';

@Component({
  selector: 'detail-item-integer',
  templateUrl: './detail-item-integer.component.html',
  styleUrls: ['./detail-item-integer.component.scss']
})
export class DetailItemIntegerComponent {

  @Input() item: DetailViewItem = null;
  @Input() includeComment = false;
  @Input() isAppAdmin = false;
  @Input() displayOldVal = false;
  @ViewChild('buttonComponent', {static: false}) public buttonComponent?;

}
