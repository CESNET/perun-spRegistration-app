import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from 'rxjs';
import { Request } from '../models/Request';
import {AuditLog} from "../models/AuditLog";

@Injectable({
  providedIn: 'root'
})
export class AuditService {

  constructor(
    private apiService: ApiService
  ) { }

  getAllAudits(): Observable<AuditLog[]> {
    return this.apiService.get('/audit');
  }

  getAuditsForRequest(id: number): Observable<Request[]> {
    return this.apiService.get(`/audit/request/${id}`);
  }

  getAuditsForService(id: number): Observable<Request[]> {
    return this.apiService.get(`/audit/facility/${id}`);
  }

}
