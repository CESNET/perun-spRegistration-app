import {Component, Input, ViewChild} from '@angular/core';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';
@Component({
  selector: 'detail-item-string',
  templateUrl: './detail-item-string.component.html',
  styleUrls: ['./detail-item-string.component.scss']
})
export class DetailItemStringComponent {

  @Input() item: DetailViewItem = null;
  @Input() includeComment = false;
  @Input() isAppAdmin = false;
  @Input() displayOldVal = false;
  @ViewChild('buttonComponent', {static: false}) public buttonComponent?;

}
