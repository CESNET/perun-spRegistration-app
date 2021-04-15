import {Pipe, PipeTransform} from '@angular/core';
import {RequestStatus} from "../core/models/enums/RequestStatus";

@Pipe({
  name: 'requestStatusLang',
  pure: false
})
export class RequestStatusLangPipe implements PipeTransform {

  constructor() { }

  transform(value: RequestStatus, args?: any): any {
    switch(value) {
      case RequestStatus.APPROVED: {
        return'REQUESTS.STATUSPIPE.APPROVED';
      }
      case RequestStatus.REJECTED: {
        return'REQUESTS.STATUSPIPE.REJECTED';
      }
      case RequestStatus.WAITING_FOR_APPROVAL: {
        return'REQUESTS.STATUSPIPE.WFA';
      }
      case RequestStatus.WAITING_FOR_CHANGES: {
        return'REQUESTS.STATUSPIPE.WFC';
      }
      case RequestStatus.CANCELED: {
        return'REQUESTS.STATUSPIPE.CANCELED';
      }
      case RequestStatus.UNKNOWN: {
        return'REQUESTS.STATUSPIPE.UNKNOWN';
      }
      default: {
        return '';
      }
    }
  }

}
