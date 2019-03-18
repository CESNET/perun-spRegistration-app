import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import { Request } from "../models/Request";
import {PerunAttribute} from "../models/PerunAttribute";

@Injectable({
  providedIn: 'root'
})
export class RequestsService {

  constructor(
    private apiService: ApiService
  ) { }

  getAllRequests(): Observable<Request[]> {
    return this.apiService.get('/allRequests');
  }

  getUserRequests(): Observable<Request[]> {
    return this.apiService.get('/userRequests');
  }

  createRegistrationRequest(perunAttributes: PerunAttribute[]): Observable<number> {
    return this.apiService.post('/register', perunAttributes);
  }

  registerAndSubmit(perunAttributes: PerunAttribute[]): Observable<number> {
    return this.apiService.post('/registerAndSubmit', perunAttributes);
  }

  getRequest(id: number): Observable<Request> {
    return this.apiService.get(`/request/${id}`);
  }

  approveRequest(id: number): Observable<boolean>{
    return this.apiService.get(`/approve/${id}`);
  }

  rejectRequest(id: number): Observable<boolean>{
    return this.apiService.post(`/reject/${id}`);
  }

  askForChanges(id: number, attributes: Iterable<PerunAttribute>): Observable<boolean>{
    return this.apiService.post(`/askForChanges/${id}`, attributes);
  }

  askForApproval(id: number ): Observable<boolean> {
    return this.apiService.get(`/askApproval/${id}`);
  }
}
