import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'requestActionPipe',
  pure: false
})
export class RequestActionPipe implements PipeTransform {

  private returnData: string = "";

  constructor(
    private translate: TranslateService
  ) { }

  transform(value: any, args?: any): any {
    switch(value) {
      case "REGISTER_NEW_SP": {
        this.translate.get('REQUESTS.ACTIONPIPE.REGISTER_NEW_SP').subscribe(response =>{
          this.returnData = response;
        });
        break;
      }
      case "UPDATE_FACILITY": {
        this.translate.get('REQUESTS.ACTIONPIPE.UPDATE_FACILITY').subscribe(response =>{
          this.returnData = response;
        });
        break;
      }
      case "DELETE_FACILITY": {
        this.translate.get('REQUESTS.ACTIONPIPE.DELETE_FACILITY').subscribe(response =>{
          this.returnData = response;
        });
        break;
      }
      case "MOVE_TO_PRODUCTION": {
        this.translate.get('REQUESTS.ACTIONPIPE.MOVE_TO_PRODUCTION').subscribe(response =>{
          this.returnData = response;
        });
        break;
      }
      default: {
        this.returnData = value;
        break;
      }
    }
    return this.returnData;
  }

}
