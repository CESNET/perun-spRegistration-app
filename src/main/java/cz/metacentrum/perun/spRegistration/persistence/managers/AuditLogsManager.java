package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLogDTO;
import lombok.NonNull;

import java.util.List;

public interface AuditLogsManager {

    AuditLogDTO insert(@NonNull AuditLogDTO log) throws InternalErrorException;

    List<AuditLogDTO> getAll();

    AuditLogDTO getById(@NonNull Long id);

    List<AuditLogDTO> getForRequest(@NonNull Long requestId);

    List<AuditLogDTO> getForFacility(@NonNull Long facilityId);

}
