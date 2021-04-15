import { Pipe, PipeTransform } from '@angular/core';
import {RequestAction} from "../core/models/enums/RequestAction";

@Pipe({
  name: 'requestAction',
  pure: false
})
export class RequestsActionPipe implements PipeTransform {

  private returnData: string = "";

  constructor() { }

  transform(value: any, args?: any): any {
    let key = '';
    switch(value) {
      case RequestAction.REGISTER_NEW_SP: {
        key = 'REQUESTS.ACTIONPIPE.REGISTER_NEW_SP';
        break;
      }
      case RequestAction.UPDATE_FACILITY: {
        key = 'REQUESTS.ACTIONPIPE.UPDATE_FACILITY';
        break;
      }
      case RequestAction.DELETE_FACILITY: {
        key = 'REQUESTS.ACTIONPIPE.DELETE_FACILITY';
        break;
      }
      case RequestAction.MOVE_TO_PRODUCTION: {
        key = 'REQUESTS.ACTIONPIPE.MOVE_TO_PRODUCTION';
        break;
      }
      case RequestAction.UNKNOWN: {
        key = 'REQUESTS.UNKNOWN';
        break;
      }
      default: {
        this.returnData = value;
        break;
      }
    }
    return key;
  }

}
