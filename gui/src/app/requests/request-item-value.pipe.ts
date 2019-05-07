import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'requestItemValue'
})
export class RequestItemValuePipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if(value.toString() == 'true'){
      return '<i class="material-icons">done</i>';
    }
    if(value.toString() == 'false'){
      return '<i class="material-icons">clear</i>';
    }
    if(value instanceof Array){
      let output = '';
      for(let val of value){
        if(val.toString().match("^(http|https)://")){
          output += `<div><a target="_blank" href="${val}">${val}</a></div>`;
        } else {
          output += `<div>${val}</div>`;
        }
      }
      return output;
    }
    if(value instanceof Object){
      let output = '';
      for(let key of Object.keys(value)){
        if(value[key].match("^(http|https)://")){
          output += `<div>${key} :  <a target="_blank" href="${value[key]}">${value[key]}</a></div>`;
        } else {
          output += `<div>${key} :  ${value[key]}</div>`;
        }
      }
      return output;
    }
    if(value.toString().match("^(http|https)://")){
      return `<a target="_blank" href="${value}">${value}</a>`;
    }
    return value;
  }

}
