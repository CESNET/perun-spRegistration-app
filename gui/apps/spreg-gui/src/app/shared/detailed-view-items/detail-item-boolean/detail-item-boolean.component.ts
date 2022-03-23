import {Component, Input, ViewChild} from '@angular/core';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';

@Component({
  selector: 'detail-item-boolean',
  templateUrl: './detail-item-boolean.component.html',
  styleUrls: ['./detail-item-boolean.component.scss']
})
export class DetailItemBooleanComponent {

  @Input() item: DetailViewItem = null;
  @Input() includeComment = false;
  @Input() isAppAdmin = false;
  @Input() displayOldVal = false;
  @ViewChild('buttonComponent', {static: false}) public buttonComponent?;

}
