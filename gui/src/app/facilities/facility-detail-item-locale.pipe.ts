import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'facilityDetailItemLocale'
})
export class FacilityDetailItemLocalePipe implements PipeTransform {

  constructor(private translate: TranslateService) { }

  transform(value: Map<string, string>, args?: any): any {
    const lang = this.translate.getDefaultLang();

    if (value === undefined || value === null) {
      return '-';
    }

    if (value.has(lang.toLowerCase())) {
      return value.get(lang.toLowerCase());
    }

    if (value.has('en')) {
      return value.get('en');
    } else {
      return '-';
    }
  }
}

