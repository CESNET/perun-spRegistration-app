package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

public interface RequestsService {

    Long createRegistrationRequest(@NonNull Long userId, @NonNull List<PerunAttribute> attributes)
            throws InternalErrorException;

     Long createFacilityChangesRequest(@NonNull Long facilityId, @NonNull Long userId,
                                       @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
             PerunUnknownException, PerunConnectionException;


    Long createRemovalRequest(@NonNull Long userId, @NonNull Long facilityId)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            PerunUnknownException, PerunConnectionException;


    Long createMoveToProductionRequest(@NonNull Long facilityId, @NonNull User user, @NonNull List<String> authorities)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException,
            PerunUnknownException, PerunConnectionException;

    boolean updateRequest(@NonNull Long requestId, @NonNull Long userId, @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    Request getRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    List<Request> getAllUserRequests(@NonNull Long userId) throws PerunUnknownException, PerunConnectionException;

    boolean cancelRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    List<Request> getAllRequests(@NonNull Long adminId) throws UnauthorizedActionException;

    Request getRequestForSignatureByCode(@NonNull String code)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ExpiredCodeException,
            InternalErrorException;

    boolean approveRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
            PerunConnectionException;

    boolean rejectRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

    boolean askForChanges(@NonNull Long requestId, @NonNull Long userId, @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

    List<AuditLog> getAllAuditLogs(@NonNull Long adminId) throws UnauthorizedActionException;

    AuditLog getAuditLog(@NonNull Long auditLogId, @NonNull Long userId) throws UnauthorizedActionException, InternalErrorException;

    List<AuditLog> getAuditLogsByReqId(@NonNull Long reqId, @NonNull Long adminId) throws UnauthorizedActionException;

    List<AuditLog> getAuditLogsByService(@NonNull Long facilityId, @NonNull Long adminId) throws UnauthorizedActionException, InternalErrorException;

}
