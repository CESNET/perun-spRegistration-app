import {Component, Input, ViewEncapsulation} from '@angular/core';
import {DetailViewItem} from '../../../../core/models/items/DetailViewItem';

@Component({
  selector: 'detail-item-sub-comment-user',
  templateUrl: './detail-item-sub-comment-user.component.html',
  styleUrls: ['./detail-item-sub-comment-user.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DetailItemSubCommentUserComponent {

  @Input() item: DetailViewItem;
  @Input() isAppAdmin: boolean;

}
