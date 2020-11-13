package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.common.models.AuditLog;

import java.util.List;

public interface AuditLogsManager {

    List<AuditLog> getAllAuditLogs();

    AuditLog getAuditLogById(Long auditLogId);

    List<AuditLog> getAuditLogsByReqId(Long reqId);

    List<AuditLog> getAuditLogsByService(Long serviceId);
}
