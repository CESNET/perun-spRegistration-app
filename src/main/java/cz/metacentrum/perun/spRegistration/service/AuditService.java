package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import lombok.NonNull;

import java.util.List;

public interface AuditService {

    List<AuditLog> getAllLogs(@NonNull Long adminId)
            throws UnauthorizedActionException;

    AuditLog getLogById(@NonNull Long auditLogId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException;

    List<AuditLog> getForRequest(@NonNull Long reqId, @NonNull Long adminId)
            throws UnauthorizedActionException;

    List<AuditLog> getForService(@NonNull Long serviceId, @NonNull Long adminId)
            throws UnauthorizedActionException, InternalErrorException;

    List<AuditLog> getForFacility(@NonNull Long facilityId, @NonNull Long adminId)
            throws UnauthorizedActionException, InternalErrorException;

}
