export class AuditLog {

  constructor(obj: any) {
    this.id = obj.id;
    this.requestId = obj.requestId;
    this.actorId = obj.actorId;
    this.actorName = obj.actorName;
    this.message = obj.message;
    this.madeAt = obj.madeAt;
  }

  id: number;
  requestId: number;
  actorId: number;
  actorName: number;
  message: string;
  madeAt: string;

}
