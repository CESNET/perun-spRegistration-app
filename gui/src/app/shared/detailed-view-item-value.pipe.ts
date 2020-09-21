import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: 'detailedViewItemValuePipe',
})
export class DetailedViewItemValuePipe implements PipeTransform {

  private static emailRegex: RegExp = RegExp("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

  private undefText = 'Undefined';

  constructor(private translate: TranslateService) {
    this.translate.get('UNDEFINED').subscribe(val => this.undefText = val);
  }

  transform(value: any, args?: any): any {
    if (typeof value === 'boolean') {
      if (!value) {
        return '<i class="material-icons red">clear</i>';
      } else {
        return '<i class="material-icons green">done</i>';
      }
    } else if (value instanceof Array) {
      return this.processArray(value);
    } else if (value instanceof Object) {
      return this.processObject(value);
    } else {
      return this.processStringOrNumber(value);
    }
  }

  private processStringOrNumber(value: any): any {
    let finalVal = value;
    if (!value || value === '_%UNDEFINED%_') {
      finalVal = this.undefText;
    } else {
      finalVal = DetailedViewItemValuePipe.urlify(finalVal);
      finalVal = DetailedViewItemValuePipe.mailize(finalVal);
    }

    return `<span class="text-dark text-break">${finalVal}</span>`;
  }

  private processObject(value: any): any {
    let output = '';
    if (value) {
      for (const key of Object.keys(value)) {
        let part = value[key];
        part = DetailedViewItemValuePipe.urlify(part);
        part = DetailedViewItemValuePipe.mailize(part);
        output += `<li class="text-break"><span class="text-dark">${key}:</span> ${part}</li>`;
      }
    } else {
      output = `<li class="text-break">${this.undefText}</li>`;
    }
    return `<ul class="m-0 pb-0 text-dark">${output}</ul>`;
  }

  private processArray(value: any): any {
    let output = '';
    if (value) {
      output = '';
      for (const val of value) {
        let part = val;
        part = DetailedViewItemValuePipe.urlify(part);
        part = DetailedViewItemValuePipe.mailize(part);
        output += `<li class="text-break">${part}</li>`;
      }
    } else {
      output = `<li class="text-break">${this.undefText}</li>`;
    }

    return `<ul class="m-0 pb-0 text-dark">${output}</ul>`;
  }

  private static urlify(text): string {
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    return text.replace(urlRegex, function(url) {
      return `<a class="text-dark" href="${url}" target="_blank">${url}</a>`;
    });
  }

  private static mailize(text): string {
    return text.replace(this.emailRegex, function(url) {
      return `<a href="mailto:${url}">${url}</a>`;
    });
  }

}
