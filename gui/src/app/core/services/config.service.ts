import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import {ApplicationItem} from "../models/ApplicationItem";

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

  getHeader() : Observable<string>{
    //todo type real in url
    return this.apiService.get('/config/header');
  }

  getFooter() : Observable<string>{
    //todo type real in url
    return this.apiService.get('/config/footer');
  }
}
