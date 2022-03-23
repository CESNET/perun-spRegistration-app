import {Component, Input, ViewChild} from '@angular/core';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';

@Component({
  selector: 'detail-item-array',
  templateUrl: './detail-item-array.component.html',
  styleUrls: ['./detail-item-array.component.scss']
})
export class DetailItemArrayComponent {

  @Input() item: DetailViewItem = null;
  @Input() includeComment = false;
  @Input() isAppAdmin = false;
  @Input() displayOldVal = false;
  @ViewChild('buttonComponent', {static: false}) public buttonComponent?;

}
