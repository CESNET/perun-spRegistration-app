import { Pipe, PipeTransform } from '@angular/core';
import {RequestStatus} from "../core/models/enums/RequestStatus";

@Pipe({
  name: 'requestStatusIcon',
  pure: false
})
export class RequestStatusIconPipe implements PipeTransform {

  constructor() { }

  transform(value: RequestStatus, args?: any): any {
    switch(value) {
      case RequestStatus.APPROVED: {
        return `<i class="material-icons green mb-0">done</i>`;
      }
      case RequestStatus.REJECTED:
      case RequestStatus.CANCELED: {
        return `<i class="material-icons red mb-0">clear</i>`;
      }
      case RequestStatus.WAITING_FOR_APPROVAL: {
        return `<i class="material-icons orange mb-0">hourglass_empty</i>`;
      }
      case RequestStatus.WAITING_FOR_CHANGES: {
        return `<i class="material-icons blue mb-0">cached</i>`;
      }
      case RequestStatus.UNKNOWN: {
        return `<i class="material-icons mb-0">help_outline</i>`;
      }
      default: {
        return '';
      }
    }
  }

}
