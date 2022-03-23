import {Component, Input, ViewEncapsulation} from '@angular/core';
import {DetailViewItem} from '../../../../core/models/items/DetailViewItem';
@Component({
  selector: 'detail-item-sub-comment-admin',
  templateUrl: './detail-item-sub-comment-admin.component.html',
  styleUrls: ['./detail-item-sub-comment-admin.component.scss'],
  encapsulation : ViewEncapsulation.None
})
export class DetailItemSubCommentAdminComponent {

  @Input() item: DetailViewItem;
  @Input() isAppAdmin: boolean;

}
