import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'requestSignatureDecisionIcon',
  pure: false
})
export class RequestSignatureDecisionIconPipePipe implements PipeTransform {

  constructor() { }

  transform(approved: any, args?: any): any {
    if (approved) {
      return '<i class="material-icons green">done</i>';
    } else {
      return '<i class="material-icons red">clear</i>';
    }
  }

}
