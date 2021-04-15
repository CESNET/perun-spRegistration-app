package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
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

    Long createRegistrationRequest(@NonNull User user, @NonNull List<PerunAttribute> attributes)
            throws InternalErrorException;

     Long createFacilityChangesRequest(@NonNull Long facilityId, @NonNull User user,
                                       @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
             PerunUnknownException, PerunConnectionException;


    Long createRemovalRequest(@NonNull User user, @NonNull Long facilityId)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            PerunUnknownException, PerunConnectionException;


    Long createMoveToProductionRequest(@NonNull Long facilityId, @NonNull User user, @NonNull List<String> authorities)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException,
            PerunUnknownException, PerunConnectionException;

    boolean updateRequest(@NonNull Long requestId, @NonNull User user, @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    RequestDTO getRequest(@NonNull Long requestId, @NonNull User user)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    List<RequestDTO> getAllUserRequests(@NonNull User user) throws PerunUnknownException, PerunConnectionException;

    boolean cancelRequest(@NonNull Long requestId, @NonNull User user)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
            PerunUnknownException, PerunConnectionException;

    List<RequestDTO> getAllRequests(@NonNull User user) throws UnauthorizedActionException;

    RequestDTO getRequestForSignatureByCode(@NonNull String code)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ExpiredCodeException,
            InternalErrorException;

    boolean approveRequest(@NonNull Long requestId, @NonNull User user)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
            PerunConnectionException;

    boolean rejectRequest(@NonNull Long requestId, @NonNull User user)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

    boolean askForChanges(@NonNull Long requestId, @NonNull User user, @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

}
