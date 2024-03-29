import { RequestStatus } from './enums/RequestStatus';
import { RequestAction } from './enums/RequestAction';

export class RequestOverview {
  static columns = [
    'id',
    'serviceId',
    'serviceName',
    'serviceIdentifier',
    'requesterId',
    'status',
    'action',
  ];

  constructor(item: any) {
    if (!item) {
      return;
    }

    this.id = item.id;
    this.serviceName = new Map<string, string>();
    if ('serviceName' in item && item.serviceName) {
      for (const k of Object.keys(item.serviceName)) {
        this.serviceName.set(k.toLowerCase(), item.serviceName[k]);
      }
    }
    this.serviceIdentifier = item.serviceIdentifier;
    this.requesterId = item.requesterId;
    this.status = RequestStatus[item.status as keyof typeof RequestStatus];
    this.action = RequestAction[item.action as keyof typeof RequestAction];
    this.serviceId = item.serviceId;
  }

  id: number = null;
  serviceName: Map<string, string> = new Map<string, string>();
  serviceIdentifier = '';
  serviceId: number = null; // FACILITY ID
  requesterId: number = null;
  status: RequestStatus = RequestStatus.UNKNOWN;
  action: RequestAction = RequestAction.UNKNOWN;
}
