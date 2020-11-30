import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'requestStatusLang',
  pure: false
})
export class RequestStatusLangPipe implements PipeTransform {

  constructor() { }

  transform(value: any, args?: any): any {
    switch(value) {
      case "APPROVED": {
        return'REQUESTS.STATUSPIPE.APPROVED';
      }
      case "REJECTED": {
        return'REQUESTS.STATUSPIPE.REJECTED';
      }
      case "WAITING_FOR_APPROVAL": {
        return'REQUESTS.STATUSPIPE.WFA';
      }
      case "WAITING_FOR_CHANGES": {
        return'REQUESTS.STATUSPIPE.WFC';
      }
      case "CANCELED": {
        return'REQUESTS.STATUSPIPE.CANCELED';
      }
      default: {
        return '';
      }
    }
  }

}
