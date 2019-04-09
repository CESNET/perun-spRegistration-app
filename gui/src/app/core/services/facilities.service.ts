import { Injectable } from '@angular/core';
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import { Facility } from "../models/Facility";
import { Request } from "../models/Request";

@Injectable({
  providedIn: 'root'
})
export class FacilitiesService {

  constructor(
    private apiService: ApiService
  ) { }

  getAllFacilities(): Observable<Facility[]> {
    return this.apiService.get('/allFacilities');
  }

  getMyFacilities(): Observable<Facility[]> {
    return this.apiService.get('/userFacilities');
  }

  getFacility(id: number): Observable<Facility> {
    return this.apiService.get('/facility/' + id);
  }

  createRequest(id: number, emails: string[]): Observable<number> {
      return this.apiService.post('/moveToProduction/createRequest/' + id, emails);
  }

  getRequestDetailsWithHash(hash: string): Observable<Request> {
    return this.apiService.get('/moveToProduction/getFacilityDetails/?code=' + hash);
  }

  signApprovalForProduction(hash: string): Observable<boolean> {
    return this.apiService.post('/moveToProduction/approve', hash);
  }

  removeFacility(id: number): Observable<number> {
    return this.apiService.post('/remove/' + id);
  }

  addAdmins(id: number, emails: string[]): Observable<boolean> {
    return this.apiService.post('/addAdmins/' + id, emails);
  }

  addAdminConfirm(hash: string): Observable<boolean> {
    return this.apiService.post('/addAdmin/confirm', hash);
  }
}
