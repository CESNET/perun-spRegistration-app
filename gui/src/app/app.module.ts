import { BrowserModule, HammerModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { FacilitiesModule } from './facilities/facilities.module';
import { MainMenuModule } from './main-menu/main-menu.module';
import {
  HTTP_INTERCEPTORS,
  HttpClient,
  HttpClientModule,
  HttpClientXsrfModule,
} from '@angular/common/http';
import { SharedModule } from './shared/shared.module';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LoginComponent } from './login/login.component';
import { DocumentSignComponent } from './document-sign/document-sign.component';
import { ToolsComponent } from './tools/tools.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { AttributeValuePipe } from './attribute-value.pipe';
import { DocumentSignItemLocalePipe } from './document-sign-item-locale.pipe';
import { LanguageEntryPipe } from './language-entry.pipe';
import { HttpXSRFInterceptor } from './core/interceptors/httpxsrf.interceptor';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DocumentSignComponent,
    ToolsComponent,
    AttributeValuePipe,
    DocumentSignItemLocalePipe,
    LanguageEntryPipe,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    CoreModule,
    FacilitiesModule,
    HammerModule,
    HttpClientModule,
    HttpClientXsrfModule,
    MainMenuModule,
    MatDialogModule,
    MatTabsModule,
    SharedModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient],
      },
    }),
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpXSRFInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
