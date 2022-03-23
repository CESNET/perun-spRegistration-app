import {Component, Input} from '@angular/core';
import {DetailItemSubCommentButtonComponent} from './detail-item-sub-comment-button/detail-item-sub-comment-button.component';
import {DetailViewItem} from '../../../core/models/items/DetailViewItem';
@Component({
  selector: 'detail-item-sub-comment',
  templateUrl: './detail-item-sub-comment.component.html',
  styleUrls: ['./detail-item-sub-comment.component.scss']
})
export class DetailItemSubCommentComponent {

  @Input() public buttonComponent: DetailItemSubCommentButtonComponent;
  @Input() item: DetailViewItem = undefined;
  @Input() isAppAdmin = false;
  @Input() includeComment = false;

}
