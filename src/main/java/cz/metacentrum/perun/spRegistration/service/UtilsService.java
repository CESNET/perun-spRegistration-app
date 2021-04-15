package cz.metacentrum.perun.spRegistration.service;

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
import java.security.InvalidKeyException;

public interface UtilsService {

    PerunAttribute regenerateClientSecret(@NonNull Long userId, @NonNull Long facilityId)
            throws UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            PerunUnknownException, PerunConnectionException;

    boolean validateCode(@NonNull String code)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException;

    PerunAttribute generateClientSecretAttribute()
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException;

    boolean isAdminForFacility(@NonNull Long facilityId, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException;

    boolean isAdminForFacility(@NonNull Long facilityId, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException;

    boolean isAdminForRequest(@NonNull RequestDTO request, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException;;

    boolean isAdminForRequest(@NonNull Long reqId, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException, InternalErrorException;

    boolean isAdminForRequest(@NonNull Long reqId, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException, InternalErrorException;

    boolean isAdminForRequest(@NonNull RequestDTO request, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException;

    boolean isAppAdmin(@NonNull Long userId);

    boolean isAppAdmin(@NonNull User user);

}
