import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'requestStatusPipe',
  pure: false
})
export class RequestStatusPipe implements PipeTransform {

  private returnData: string = "";

  constructor(
    private translate: TranslateService
  ) { }

  transform(value: any, args?: any): any {
    switch(value) {
      case "APPROVED": {
        this.translate.get('REQUESTS.STATUSPIPE.APPROVED').subscribe(response =>{
          this.returnData = `<i class="material-icons green mb-0">done</i>${response}`;
        });
        break;
      }
      case "REJECTED": {
        this.translate.get('REQUESTS.STATUSPIPE.REJECTED').subscribe(response =>{
          this.returnData = `<i class="material-icons red">clear</i>${response}`;
        });
        break;
      }
      case "WAITING_FOR_APPROVAL": {
        this.translate.get('REQUESTS.STATUSPIPE.WFA').subscribe(response =>{
          this.returnData = `<i class="material-icons orange ">hourglass_empty</i>${response}`;
        });
        break;
      }
      case "WAITING_FOR_CHANGES": {
        this.translate.get('REQUESTS.STATUSPIPE.WFC').subscribe(response =>{
          this.returnData = `<i class="material-icons blue">cached</i>${response}`;
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
