import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'requestDetailItemLocale'
})
export class RequestDetailItemLocalePipe implements PipeTransform {

  constructor(private translate: TranslateService) {  }

  transform(value: Map<string, string>, args?: any): any {
    const lang = this.translate.getDefaultLang();
    if (!value || value.size === 0) {
      return '-';
    }
    if (value.has(lang.toLowerCase())) {
      return value.get(lang.toLowerCase());
    }
    return value.get('en');
  }
}

