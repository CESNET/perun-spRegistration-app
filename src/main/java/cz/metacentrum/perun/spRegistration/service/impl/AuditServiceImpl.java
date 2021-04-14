package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.managers.AuditLogsManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.service.AuditService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {
    
    private final AuditLogsManager manager;
    private final ProvidedServiceManager providedServiceManager;

    @Autowired
    public AuditServiceImpl(AuditLogsManager manager, ProvidedServiceManager providedServiceManager) {
        this.manager = manager;
        this.providedServiceManager = providedServiceManager;
    }

    @Override
    public List<AuditLog> getAllLogs(@NonNull Long adminId) {
        return manager.getAll();
    }

    @Override
    public AuditLog getLogById(@NonNull Long auditLogId, @NonNull Long userId) throws InternalErrorException {
        AuditLog auditLog = manager.getById(auditLogId);
        if (auditLog == null) {
            log.error("Could not retrieve audit log for id: {}", auditLogId);
            throw new InternalErrorException("Could not retrieve audit log for id: " + auditLogId);
        }

        return auditLog;
    }

    @Override
    public List<AuditLog> getForRequest(@NonNull Long reqId, @NonNull Long adminId) {
        return manager.getForRequest(reqId);
    }

    @Override
    public List<AuditLog> getForFacility(@NonNull Long facilityId, @NonNull Long adminId)
            throws InternalErrorException
    {
        ProvidedService service = providedServiceManager.getByFacilityId(facilityId);
        if (service == null) {
            throw new IllegalArgumentException();
        }
        return manager.getForFacility(service.getId());
    }

}
