import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from 'rxjs';
import { Request } from '../models/Request';
import { PerunAttribute } from '../models/PerunAttribute';
import { RequestSignature } from '../models/RequestSignature';
import {RequestOverview} from "../models/RequestOverview";

@Injectable({
  providedIn: 'root'
})
export class RequestsService {

  private static prefix = "/request";

  constructor(
    private apiService: ApiService
  ) { }

  getAllRequests(): Observable<RequestOverview[]> {
    return this.apiService.get(RequestsService.prefix);
  }

  getUserRequests(): Observable<RequestOverview[]> {
    return this.apiService.get(RequestsService.prefix + '/user');
  }

  createRegistrationRequest(perunAttributes: PerunAttribute[]): Observable<number> {
    return this.apiService.post(RequestsService.prefix +'/register', perunAttributes);
  }

  getRequest(id: number): Observable<Request> {
    return this.apiService.get(RequestsService.prefix + `/request/${id}`);
  }

  approveRequest(id: number): Observable<boolean>{
    return this.apiService.post(RequestsService.prefix + `/approve/${id}`);
  }

  rejectRequest(id: number): Observable<boolean>{
    return this.apiService.post(RequestsService.prefix + `/reject/${id}`);
  }

  askForChanges(id: number, attributes: Iterable<PerunAttribute>): Observable<boolean>{
    return this.apiService.post(RequestsService.prefix + `/askForChanges/${id}`, attributes);
  }

  askForApproval(id: number ): Observable<boolean> {
    return this.apiService.get(RequestsService.prefix + `/askApproval/${id}`);
  }

  updateRequest(id: number, attributes: PerunAttribute[]): Observable<boolean>{
    return this.apiService.post(RequestsService.prefix + `/update/${id}`, attributes)
  }

  cancelRequest(id: number): Observable<boolean>{
    return this.apiService.post(RequestsService.prefix + `/cancel/${id}`);
  }

  getSignatures(id: number): Observable<RequestSignature[]> {
    return this.apiService.get(RequestsService.prefix + `/${id}/signatures`);
  }

}
