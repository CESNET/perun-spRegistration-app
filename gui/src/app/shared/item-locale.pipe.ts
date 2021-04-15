import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'itemLocale',
  pure: false
})
export class ItemLocalePipe implements PipeTransform {

  constructor(private translate: TranslateService) {  }

  transform(value: Map<string, string>, args?: any): any {
    if (!value) {
      return '-';
    }
    if (value.has(this.translate.currentLang.toLowerCase())) {
      return value.get(this.translate.currentLang.toLowerCase());
    }
    return value.get(this.translate.defaultLang.toLowerCase());
  }

}

