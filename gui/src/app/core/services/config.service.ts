import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import {Observable, of} from "rxjs";
import {ApplicationItem} from "../models/ApplicationItem";
import {PageConfig} from "../models/PageConfig";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(
    private apiService: ApiService
  ) { }

  isOidcEnabled(): Observable<boolean> {
    return this.apiService.get('/config/oidcEnabled');
  }

  getSamlApplicationItems() : Observable<ApplicationItem[][]> {
    return this.apiService.get('/config/samlInputs');
  }

  getOidcApplicationItems() : Observable<ApplicationItem[][]> {
    return this.apiService.get('/config/oidcInputs');
  }

  getPageConfig() : Observable<PageConfig> {
      return this.apiService.get('/config/pageConfig');
  }

  isUserAdmin() : Observable<boolean> {
    return this.apiService.get('/config/isUserAdmin');
  }
}
