import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { FacilitiesModule } from './facilities/facilities.module';
import { MainMenuModule } from './main-menu/main-menu.module';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { SharedModule } from './shared/shared.module';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LoginComponent } from './login/login.component';
import { DocumentSignComponent } from './document-sign/document-sign.component';
import {ToolsComponent} from './tools/tools.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {AttributeValuePipe} from './attribute-value.pipe';
import {DocumentSignItemLocalePipe} from "./document-sign-item-locale.pipe";
import {LanguageEntryPipe} from "./language-entry.pipe";
import { HammerModule} from '@angular/platform-browser';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import {StatisticsModule} from "./statistics/statistics.module";
import {ConfService} from "./shared/conf-service";
import {map} from "rxjs";

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

function initialize(http: HttpClient, config: ConfService) {
  return (): Promise<boolean> => {
    return new Promise<boolean>((resolve: (a: boolean) => void): void => {
      http.get('/assets/config/config.json')
        .pipe(
          map((x: ConfService) => {
            config.statisticsDisplayedAttributes = x.statisticsDisplayedAttributes;
            resolve(true);
          })
        ).subscribe();
    });
  };
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DocumentSignComponent,
    ToolsComponent,
    AttributeValuePipe,
    DocumentSignItemLocalePipe,
    LanguageEntryPipe
  ],
  imports: [
      AppRoutingModule,
      BrowserModule,
      CoreModule,
      FacilitiesModule,
      HttpClientModule,
      MainMenuModule,
      SharedModule,
      TranslateModule.forRoot({
          loader: {
              provide: TranslateLoader,
              useFactory: HttpLoaderFactory,
              deps: [HttpClient]
          }
      }),
      BrowserAnimationsModule,
      MatDialogModule,
      MatTabsModule,
      HammerModule,
      StatisticsModule,
      NgxChartsModule
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initialize,
      deps: [
        HttpClient,
        ConfService
      ],
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
