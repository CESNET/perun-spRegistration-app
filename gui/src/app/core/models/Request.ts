import {AttributesEntity} from './AttributesEntity';
import {User} from './User';
import {ProvidedService} from "./ProvidedService";
import {RequestAction} from "./enums/RequestAction";
import {RequestStatus} from "./enums/RequestStatus";

export class Request extends AttributesEntity {
  constructor(item: any) {
    if (!item) {
      return;
    }

    super(item.attributes);
    this.reqId = item.reqId;
    this.facilityId = item.facilityId;
    this.status = RequestStatus[item.status as keyof typeof RequestStatus];
    this.action = RequestAction[item.action as keyof typeof RequestAction];
    this.reqUserId = item.reqUserId;
    this.modifiedAt = item.modifiedAt;
    this.modifiedBy = item.modifiedBy;
    this.requester = new User(item.requester);
    this.modifier = new User(item.modifier);
    if (item.providedService) {
      this.providedService = new ProvidedService(item.providedService);
    } else {
      this.providedService = null;
    }
  }

  reqId: number;
  facilityId: number;
  status: RequestStatus;
  action: RequestAction;
  reqUserId: number;
  modifiedAt: string;
  modifiedBy: number;
  requester: User;
  modifier: User;
  providedService: ProvidedService;

}
