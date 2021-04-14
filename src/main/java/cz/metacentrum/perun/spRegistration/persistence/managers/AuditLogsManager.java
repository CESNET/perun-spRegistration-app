package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import lombok.NonNull;

import java.util.List;

public interface AuditLogsManager {

    AuditLog insert(@NonNull AuditLog log) throws InternalErrorException;

    List<AuditLog> getAll();

    AuditLog getById(@NonNull Long id);

    List<AuditLog> getForRequest(@NonNull Long requestId);

    List<AuditLog> getForFacility(@NonNull Long facilityId);

}
