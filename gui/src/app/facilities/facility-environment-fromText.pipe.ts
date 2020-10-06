import {OnInit, Pipe, PipeTransform} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {switchAll} from "rxjs/operators";

@Pipe({
  name: 'facilityEnvironmentFromText'
})
export class FacilityEnvironmentFromTextPipe implements PipeTransform {

  private testEnvText: string = "TEXT";
  private prodEnvText: string = "TEXT2";

  constructor(
    private translate: TranslateService
  ) {
    this.translate.get('FACILITIES.ENV_TEST').subscribe(text => {
      this.testEnvText = text;
    });
    this.translate.get('FACILITIES.ENV_PROD').subscribe(text => {
      this.prodEnvText = text;
    });
  }

  transform(env: any, args?: any): any {
    switch(env) {
      case "TESTING": return this.testEnvText;
      case "PRODUCTION": return this.prodEnvText;
    }
  }

}
