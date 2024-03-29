import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Facility } from '../models/Facility';
import { Request } from '../models/Request';
import { PerunAttribute } from '../models/PerunAttribute';
import { LinkCode } from '../models/LinkCode';
import { ProvidedService } from '../models/ProvidedService';

@Injectable({
  providedIn: 'root',
})
export class FacilitiesService {
  constructor(private apiService: ApiService) {}

  getAllFacilities(): Observable<ProvidedService[]> {
    return this.apiService.get('/allFacilities');
  }

  getAllExternalFacilities(): Observable<ProvidedService[]> {
    return this.apiService.get('/allFacilitiesExternal');
  }

  getMyFacilities(): Observable<ProvidedService[]> {
    return this.apiService.get('/userFacilities');
  }

  getMyExternalFacilities(): Observable<ProvidedService[]> {
    return this.apiService.get('/userFacilitiesExternal');
  }

  getFacility(id: number): Observable<Facility> {
    return this.apiService.get('/facility/' + id);
  }

  getFacilitySignature(id: number): Observable<Facility> {
    return this.apiService.get('/facility/signature/' + id);
  }

  createTransferApprovalRequest(
    id: number,
    emails: string[]
  ): Observable<number> {
    return this.apiService.post(
      '/moveToProduction/approveRequest/' + id,
      emails
    );
  }

  getRequestDetailsWithHash(hash: string): Observable<Request> {
    return this.apiService.get(
      '/moveToProduction/getFacilityDetails/?code=' + hash
    );
  }

  approveTransferToProduction(hash: string): Observable<boolean> {
    return this.apiService.post('/moveToProduction/approve', hash);
  }

  rejectTransferToProduction(hash: string): Observable<boolean> {
    return this.apiService.post('/moveToProduction/reject', hash);
  }

  removeFacility(id: number): Observable<number> {
    return this.apiService.post('/request/remove/' + id);
  }

  addAdmins(id: number, emails: string[]): Observable<boolean> {
    return this.apiService.post('/addAdmins/' + id, emails);
  }

  addAdminConfirm(hash: string): Observable<boolean> {
    return this.apiService.post('/addAdmin/confirm', hash);
  }

  addAdminReject(hash: string): Observable<boolean> {
    return this.apiService.post('/addAdmin/reject', hash);
  }

  addAdminGetDetails(hash: string): Observable<LinkCode> {
    return this.apiService.get('/addAdmin/getDetails/' + hash);
  }

  addAdminGetFacilityDetails(faciltiyId: number): Observable<Facility> {
    return this.apiService.get('/addAdmin/getFacilityDetails/' + faciltiyId);
  }

  changeFacility(
    id: number,
    perunAttributes: PerunAttribute[]
  ): Observable<number> {
    return this.apiService.post(
      '/request/changeFacility/' + id,
      perunAttributes
    );
  }

  getFacilityWithInputs(id: number): Observable<Facility> {
    return this.apiService.get('/facilityWithInputs/' + id);
  }

  regenerateClientSecret(id: number): Observable<PerunAttribute> {
    return this.apiService.post('/facility/regenerateClientSecret/' + id);
  }

  removeAdmin(facilityId: number, adminId: number): Observable<boolean> {
    return this.apiService.post('/removeAdmin/' + facilityId, adminId);
  }
}
