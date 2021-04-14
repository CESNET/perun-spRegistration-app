package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import cz.metacentrum.perun.spRegistration.service.impl.AuditServiceImpl;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController(value = "/api/audit")
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
        if (!utilsService.isAppAdmin(user)) {
            throw new UnauthorizedActionException();
        }
        return auditService.getAllLogs(user.getId());
    }

    @GetMapping(path = "/request/{reqId}")
    public List<AuditLog> auditLogsByReqId(@NonNull @SessionAttribute("user") User user,
                                           @NonNull @PathVariable("reqId") Long reqId)
            throws UnauthorizedActionException
    {
        if (!utilsService.isAppAdmin(user)) {
            throw new UnauthorizedActionException();
        }
        return auditService.getForRequest(reqId, user.getId());
    }

    @GetMapping(path = "/facility/{facilityId}")
    public List<AuditLog> auditLogsByService(@NonNull @SessionAttribute("user") User user,
                                             @NonNull @PathVariable("facilityId") Long facilityId)
            throws UnauthorizedActionException, InternalErrorException {
        if (!utilsService.isAppAdmin(user)) {
            throw new UnauthorizedActionException();
        }
        return auditService.getForFacility(facilityId, user.getId());
    }
}
