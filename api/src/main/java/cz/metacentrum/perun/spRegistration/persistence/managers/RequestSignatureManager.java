package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;

import java.util.List;

public interface RequestSignatureManager {

    /**
     * Add signature for moving to production
     * @param requestId id of request to be signed
     * @param userId id of user giving the signature
     * @param userName name of signing user
     * @param approved TRUE if approved, FALSE if rejected
     * @param code code for signature
     * @return True if everything went OK
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL, when param
     * "code" is NULL or empty.
     */
    boolean addSignature(Long requestId, Long userId, String userName, boolean approved, String code)
            throws InternalErrorException;

    /**
     * Get all approvals for transferring of service into production environment
     * @param requestId id of transfer request
     * @return List of associated approvals
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL.
     */
    List<RequestSignature> getRequestSignatures(Long requestId);

}