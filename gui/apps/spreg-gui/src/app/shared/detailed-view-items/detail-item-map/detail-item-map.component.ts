import {Component, Input, ViewChild} from '@angular/core';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';

@Component({
  selector: 'detail-item-map',
  templateUrl: './detail-item-map.component.html',
  styleUrls: ['./detail-item-map.component.scss']
})
export class DetailItemMapComponent {

  @Input() item: DetailViewItem = null;
  @Input() includeComment = false;
  @Input() isAppAdmin = false;
  @Input() displayOldVal = false;
  @ViewChild('buttonComponent', {static: false}) public buttonComponent?;

}
