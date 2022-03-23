import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import {Observable} from 'rxjs';
import {ApplicationItem} from '../models/ApplicationItem';
import {PageConfig} from '../models/PageConfig';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(
    private apiService: ApiService
  ) { }

  getProtocolsEnabled(): Observable<string[]> {
    return this.apiService.get('/config/protocols');
  }

  getSamlApplicationItems(): Observable<ApplicationItem[][]> {
    return this.apiService.get('/config/samlInputs');
  }

  getOidcApplicationItems(): Observable<ApplicationItem[][]> {
    return this.apiService.get('/config/oidcInputs');
  }

  getPageConfig(): Observable<PageConfig> {
      return this.apiService.get('/config/pageConfig');
  }

  isUserAdmin(): Observable<boolean> {
    return this.apiService.get('/config/isApplicationAdmin');
  }

  isAuthoritiesEnabled(): Observable<boolean> {
      return this.apiService.get('/config/specifyAuthoritiesEnabled');
  }

  getProdTransferEntries(): Observable<string[]> {
    return this.apiService.get('/config/prodTransferEntries');
  }

  getLanguages(): Observable<string[]> {
    return this.apiService.get('/config/langs');
  }
}
