import {RequestStatus} from "./enums/RequestStatus";
import {RequestAction} from "./enums/RequestAction";

export class RequestOverview {

  static columns = ['id', 'serviceName', 'serviceIdentifier', 'requesterId', 'status', 'action'];

  constructor(item: any) {
    if (!item) {
      return;
    }

    this.id = item.id;
    this.serviceName = new Map<string, string>();
    if (item.hasOwnProperty('serviceName') && item.serviceName) {
      for (const k of Object.keys(item.serviceName)) {
        this.serviceName.set(k.toLowerCase(), item.serviceName[k]);
      }
    }
    this.serviceIdentifier = item.serviceIdentifier;
    this.requesterId = item.requesterId;
    this.status = RequestStatus[item.status as keyof typeof RequestStatus];
    this.action = RequestAction[item.action as keyof typeof RequestAction];
    this.facilityId = item.facilityId;
  }

  id: number = null;
  serviceName: Map<string, string> = new Map<string, string>();
  serviceIdentifier: string = '';
  facilityId: number = null;
  requesterId: number = null;
  status: RequestStatus = RequestStatus.UNKNOWN;
  action: RequestAction = RequestAction.UNKNOWN;

}
