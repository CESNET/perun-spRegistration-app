package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignatureDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import java.security.InvalidKeyException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.NonNull;

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
    List<RequestSignatureDTO> getSignaturesForRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

}
