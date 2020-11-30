import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'requestStatusIcon',
  pure: false
})
export class RequestStatusIconPipe implements PipeTransform {

  constructor() { }

  transform(value: any, args?: any): any {
    switch(value) {
      case "APPROVED": {
        return `<i class="material-icons green mb-0">done</i>`;
      }
      case "REJECTED":
      case "CANCELED": {
        return `<i class="material-icons red mb-0">clear</i>`;
      }
      case "WAITING_FOR_APPROVAL": {
        return `<i class="material-icons orange mb-0">hourglass_empty</i>`;
      }
      case "WAITING_FOR_CHANGES": {
        return `<i class="material-icons blue mb-0">cached</i>`;
      }
      default: {
        return '';
      }
    }
  }

}
