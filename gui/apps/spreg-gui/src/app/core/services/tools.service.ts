import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import {Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ToolsService {

  constructor(
    private apiService: ApiService
  ) { }

  encrypt(toEncrypt: String): Observable<Map<string, string>> {
    return this.apiService.post('/tools/encrypt', toEncrypt);
  }

  decrypt(toDecrypt: String): Observable<string> {
    return this.apiService.post('/tools/decrypt', toDecrypt);
  }
}
