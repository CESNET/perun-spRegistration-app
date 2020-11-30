import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from 'rxjs';
import { Request } from '../models/Request';
import { PerunAttribute } from '../models/PerunAttribute';
import { RequestSignature } from '../models/RequestSignature';

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

  getRequest(id: number): Observable<Request> {
    return this.apiService.get(`/request/${id}`);
  }

  approveRequest(id: number): Observable<boolean>{
    return this.apiService.post(`/approve/${id}`);
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

  updateRequest(id: number, attributes: PerunAttribute[]): Observable<boolean>{
    return this.apiService.post(`/update/${id}`, attributes)
  }

  getSignatures(id: number): Observable<RequestSignature[]> {
    return this.apiService.get(`/viewApprovals/${id}`);
  }

  cancelRequest(id: number): Observable<boolean>{
    return this.apiService.post(`/cancel/${id}`);
  }

}
