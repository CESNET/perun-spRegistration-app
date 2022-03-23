import {Component, Input} from '@angular/core';
@Component({
  selector: 'detail-item-sub-comment-button',
  templateUrl: './detail-item-sub-comment-button.component.html',
  styleUrls: ['./detail-item-sub-comment-button.component.scss']
})
export class DetailItemSubCommentButtonComponent {

  @Input() isAppAdmin = false;
  @Input() showComment = false;
  @Input() includeComment = false;

  toggleComment(): void {
    this.showComment = !this.showComment;
  }

}
