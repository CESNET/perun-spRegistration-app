package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLogDTO;
import lombok.NonNull;

import java.util.List;

public interface AuditService {

    List<AuditLogDTO> getAllLogs(@NonNull Long adminId)
            throws UnauthorizedActionException;

    AuditLogDTO getLogById(@NonNull Long auditLogId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException;

    List<AuditLogDTO> getForRequest(@NonNull Long reqId, @NonNull Long adminId)
            throws UnauthorizedActionException;

    List<AuditLogDTO> getForService(@NonNull Long serviceId, @NonNull Long adminId)
            throws UnauthorizedActionException, InternalErrorException;

    List<AuditLogDTO> getForFacility(@NonNull Long facilityId, @NonNull Long adminId)
            throws UnauthorizedActionException, InternalErrorException;

}
