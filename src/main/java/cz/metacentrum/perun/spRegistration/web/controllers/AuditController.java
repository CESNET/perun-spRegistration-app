package cz.metacentrum.perun.spRegistration.web.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLogDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import cz.metacentrum.perun.spRegistration.service.impl.AuditServiceImpl;
import cz.metacentrum.perun.spRegistration.web.ApiEntityMapper;
import cz.metacentrum.perun.spRegistration.web.ApiUtils;
import cz.metacentrum.perun.spRegistration.web.models.AuditLog;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping(value = "/api/audit")
@Slf4j
public class AuditController {

    @NonNull private final AuditServiceImpl auditService;
    @NonNull private final UtilsService utilsService;

    @Autowired
    public AuditController(@NonNull AuditServiceImpl auditService, @NonNull UtilsService utilsService) {
        this.auditService = auditService;
        this.utilsService = utilsService;
    }

    @GetMapping
    public List<AuditLog> allAuditLogs(@NonNull @SessionAttribute("user") User user)
            throws UnauthorizedActionException
    {
        if (!ApiUtils.isAppAdmin()) {
            throw new UnauthorizedActionException();
        }
        List<AuditLogDTO> auditList = auditService.getAllLogs(user.getId());
        return ApiEntityMapper.mapAuditLogDTOstoAuditLog(auditList);
    }

    @GetMapping(path = "/request/{reqId}")
    public List<AuditLog> auditLogsByReqId(@NonNull @SessionAttribute("user") User user,
                                              @NonNull @PathVariable("reqId") Long reqId)
            throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException, InternalErrorException
    {
        if (!utilsService.isAdminForRequest(reqId, user)) {
            throw new UnauthorizedActionException();
        }
        List<AuditLogDTO> auditList = auditService.getForRequest(reqId, user.getId());
        return ApiEntityMapper.mapAuditLogDTOstoAuditLog(auditList);
    }

    @GetMapping(path = "/facility/{facilityId}")
    public List<AuditLog> auditLogsByService(@NonNull @SessionAttribute("user") User user,
                                                @NonNull @PathVariable("facilityId") Long facilityId)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        if (!utilsService.isAdminForFacility(facilityId, user)) {
            throw new UnauthorizedActionException();
        }
        List<AuditLogDTO> auditList = auditService.getForFacility(facilityId, user.getId());
        return ApiEntityMapper.mapAuditLogDTOstoAuditLog(auditList);
    }
}
