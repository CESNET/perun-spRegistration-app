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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

public interface AddAdminsService {

    /**
     * Add users as admins (managers) for facility in Perun.
     * @param user User performing the action
     * @param facilityId ID of facility in Perun.
     * @param admins List of emails to whom the notification should be sent
     * @return TRUE if everything went OK, FALSE otherwise.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws BadPaddingException Thrown when cannot generate code.
     * @throws InvalidKeyException Thrown when cannot generate code.
     * @throws IllegalBlockSizeException Thrown when cannot generate code.
     * @throws UnsupportedEncodingException Thrown when cannot generate code.
     * @throws InternalErrorException Thrown when cannot find Facility for given ID.
     * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "facilityId" is NULL, when param
     * "admins" is NULL or empty.
     */
    boolean addAdminsNotify(User user, Long facilityId, List<String> admins)
            throws UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
            UnsupportedEncodingException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    /**
     * Confirm request to be added as a facility admin.
     * @param user user to be added or removed from facility admins.
     * @param code code generated for the approval
     * @return TRUE if everything went OK, FALSE otherwise.
     * @throws BadPaddingException Thrown when cannot decrypt code.
     * @throws InvalidKeyException Thrown when cannot decrypt code.
     * @throws IllegalBlockSizeException Thrown when cannot decrypt code.
     * @throws ExpiredCodeException Thrown when used code is expired.
     * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
     */
    boolean confirmAddAdmin(User user, String code)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException,
            InternalErrorException, CodeNotStoredException, PerunUnknownException, PerunConnectionException;

    /**
     * Reject request to be added as a facility admin.
     * @param user user to be added or removed from facility admins.
     * @param code code generated for the approval
     * @throws BadPaddingException Thrown when cannot decrypt code.
     * @throws InvalidKeyException Thrown when cannot decrypt code.
     * @throws IllegalBlockSizeException Thrown when cannot decrypt code.
     * @throws ExpiredCodeException Thrown when used code is expired.
     * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
     */
    void rejectAddAdmin(User user, String code) throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, ExpiredCodeException, InternalErrorException, CodeNotStoredException;

    /**
     * Get details for the given hash. The details can be then displayed on the page.
     * @param hash Hash identifying the addAdmin code
     * @return LinkCode.ts object or null
     */
    LinkCode getCodeByString(String hash);

    Facility getFacilityDetails(Long facilityId, User user) throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException;
}
