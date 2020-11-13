package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.models.User;
import lombok.NonNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

public interface RequestSignaturesService {

    boolean addSignature(@NonNull User user, @NonNull String code, @NonNull boolean approved)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ExpiredCodeException,
            InternalErrorException;

    /**
     * Get approvals for request to transfer to production.
     * @param requestId ID of request.
     * @param userId ID of user displaying the approvals.
     * @return List of approvals.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when request cannot be found in DB.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when "param" userId is NULL.
     */
    List<RequestSignature> getSignaturesForRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException;

}
