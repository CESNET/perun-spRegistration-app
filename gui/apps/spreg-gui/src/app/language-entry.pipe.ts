import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'languageEntry'
})
export class LanguageEntryPipe implements PipeTransform {

  transform(value: string, args?: any): any {
    if (value === undefined || value === null) {
      return '';
    }

    switch (value.toLowerCase()) {
      case 'en':
        return 'English';
      case 'de':
        return 'Deutsch'
      case 'cs':
        return 'Čeština';
      case 'sk':
        return 'Slovenčina';
      case 'es':
        return 'Español';
      case 'it':
        return 'Italiano';
      case 'fr':
        return 'Français';
      default:
        return '';
    }
  }
}
