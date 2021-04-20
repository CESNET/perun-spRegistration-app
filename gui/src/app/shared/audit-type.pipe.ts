import {Pipe, PipeTransform} from '@angular/core';
import {AuditLogType} from '../core/models/enums/AuditLogType';

@Pipe({
  name: 'auditTypeTranslationKey',
  pure: false
})
export class AuditTypePipe implements PipeTransform {

  private prefix = 'PIPES.AUDIT_TYPE.';

  constructor() {  }

  transform(value: AuditLogType, args?: any): any {
    switch (value) {
      case AuditLogType.REQUEST_REG_SERVICE_CREATED:
        return this.prefix + 'REQUEST_REG_SERVICE_CREATED';
      case AuditLogType.REQUEST_UPDATE_SERVICE_CREATED:
        return this.prefix + 'REQUEST_UPDATE_SERVICE_CREATED';
      case AuditLogType.REQUEST_TRANSFER_SERVICE_CREATED:
        return this.prefix + 'REQUEST_TRANSFER_SERVICE_CREATED';
      case AuditLogType.REQUEST_REMOVE_SERVICE_CREATED:
        return this.prefix + 'REQUEST_REMOVE_SERVICE_CREATED';
      case AuditLogType.REQUEST_APPROVED:
        return this.prefix + 'REQUEST_APPROVED';
      case AuditLogType.REQUEST_REJECTED:
        return this.prefix + 'REQUEST_REJECTED';
      case AuditLogType.REQUEST_CHANGES_REQUEST:
        return this.prefix + 'REQUEST_CHANGES_REQUEST';
      case AuditLogType.REQUEST_UPDATED:
        return this.prefix + 'REQUEST_UPDATED';
      case AuditLogType.REQUEST_CANCELED:
        return this.prefix + 'REQUEST_CANCELED';
      default:
        return this.prefix + 'UNKNOWN';
    }
  }

}

