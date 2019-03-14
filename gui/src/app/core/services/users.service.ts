import { Injectable } from '@angular/core';
import {ApiService} from "./api.service";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  constructor(
      private apiService: ApiService
  ) { }

  login() {
      return this.apiService.get('/setUser');
  }

  isUserAdmin() : Observable<boolean> {
      return this.apiService.get('/config/isUserAdmin');
  }
}
