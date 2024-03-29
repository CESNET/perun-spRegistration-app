import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'requestItemValue',
})
export class RequestsItemValuePipe implements PipeTransform {
  private expression =
    /^(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[^\s]{2,}|www\.[a-zA-Z0-9]+\.[^\s]{2,})$/;

  private regex = new RegExp(this.expression);

  transform(value: any, args?: any): any {
    if (typeof value === 'boolean') {
      if (!value) {
        return '<i class="material-icons red">clear</i>';
      } else {
        return '<i class="material-icons green">done</i>';
      }
    }
    if (value instanceof Array) {
      let output = '';
      for (const val of value) {
        if (val.toString().match(this.regex)) {
          output += `<li><a target="_blank" href="${val}">${val}</a></li>`;
        } else {
          output += `<li>${val}</li>`;
        }
      }
      return `<ul class="m-0 pb-0">${output}</ul>`;
    }
    if (value instanceof Object) {
      let output = '';
      for (const key of Object.keys(value)) {
        if (value[key].match(this.regex)) {
          output += `<li>${key} :  <a target="_blank" href="${value[key]}">${value[key]}</a></li>`;
        } else {
          output += `<li>${key} :  ${value[key]}</li>`;
        }
      }
      return `<ul class="m-0 pb-0">${output}</ul>`;
    }
    if (value.toString().match(this.regex)) {
      return `<a target="_blank" href="${value}">${value}</a>`;
    }
    return value;
  }
}
