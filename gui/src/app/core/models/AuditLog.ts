import {AuditLogType} from './enums/AuditLogType';

export class AuditLog {

  constructor(item: any) {
    this.id = item.id;
    this.requestId = item.requestId;
    this.actorId = item.actorId;
    this.actorName = item.actorName;
    this.type = AuditLogType[item.type as keyof typeof AuditLogType];
    this.madeAt = item.madeAt;
  }

  id: number = null;
  requestId: number = null;
  actorId: number = null;
  actorName: string = '';
  type: AuditLogType = AuditLogType.UNKNOWN;
  madeAt: string;

}
