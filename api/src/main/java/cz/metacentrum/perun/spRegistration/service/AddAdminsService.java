package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

public interface AddAdminsService {

    boolean addAdminsNotify(@NonNull User user, @NonNull Long facilityId, @NonNull List<String> admins)
            throws UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            UnsupportedEncodingException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    boolean confirmAddAdmin(@NonNull User user, @NonNull String code)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException,
            InternalErrorException, CodeNotStoredException, PerunUnknownException, PerunConnectionException;

    void rejectAddAdmin(@NonNull User user, @NonNull String code)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException,
            InternalErrorException, CodeNotStoredException;

    LinkCode getCodeByString(@NonNull String hash);

    Facility getFacilityDetails(@NonNull Long facilityId, @NonNull User user)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InternalErrorException,
            UnauthorizedActionException, PerunUnknownException, PerunConnectionException;

}
