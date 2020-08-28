package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.models.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

public interface RequestSignaturesService {

    /**
     * Add signature for transfer to production
     * @param user user signing the request
     * @param code hash of request
     * @param approved TRUE if approved, FALSE if rejected
     * @return TRUE if everything went OK, FALSE otherwise.
     * @throws BadPaddingException Thrown when cannot decode code.
     * @throws InvalidKeyException Thrown when cannot decode code.
     * @throws IllegalBlockSizeException Thrown when cannot decode code.
     * @throws InternalErrorException Thrown when request was not signed in DB.
     * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
     */
    boolean addSignature(User user, String code, boolean approved)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException, InternalErrorException;

    /**
     * Get approvals for request to transfer to production.
     * @param requestId ID of request.
     * @param userId ID of user displaying the approvals.
     * @return List of approvals.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when request cannot be found in DB.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when "param" userId is NULL.
     */
    List<RequestSignature> getSignaturesForRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, InternalErrorException;
}
