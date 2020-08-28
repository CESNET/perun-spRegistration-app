package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

public interface RequestsService {

    /**
     * Create request for Registration of SP (initial version of facility, not stored in Perun yet).
     * @param userId ID of requesting user.
     * @param attributes Attributes set for SP (key = attribute name, value = attribute).
     * @return Generated request ID after storing to the DB.
     * @throws InternalErrorException Thrown when request could not be created or was created incorrectly.
     * @throws IllegalArgumentException Thrown when param "userId" is NULL, when param "attributes" is NULL.
     */
    Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException;

    /**
     * Create request for changes of SP (which already exists as facility in Perun).
     * @param facilityId ID of facility in Perun.
     * @param userId ID of requesting user.
     * @param attributes Attributes set for SP (key = attribute name, value = attribute).
     * @return Generated request ID after storing to the DB.
     * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when cannot fetch facility with given ID,
     * when request could not be created or was created correctly.
     * @throws ActiveRequestExistsException Thrown when there already exists active request for given facility.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL, when param
     * "attributes" is NULL.
     */
    Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException, PerunUnknownException, PerunConnectionException;

    /**
     * Create request for removal of SP (which already exists as facility in Perun).
     * @param userId ID of requesting user.
     * @param facilityId ID of facility in Perun.
     * @return Generated request ID after storing to the DB.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when cannot fetch facility with given ID,
     * when request could not be created or was created correctly.
     * @throws ActiveRequestExistsException Thrown when there already exists active request for given facility.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
     */
    Long createRemovalRequest(Long userId, Long facilityId)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException, PerunUnknownException, PerunConnectionException;

    /**
     * Ask for moving the service to the production environment.
     * @param facilityId ID of facility in Perun.
     * @param user Requesting user.
     * @param authorities List to whom the emails should be sent
     * @return Id of created request
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws BadPaddingException Thrown when cannot generate code.
     * @throws InvalidKeyException Thrown when cannot generate code.
     * @throws IllegalBlockSizeException Thrown when cannot generate code.
     * @throws UnsupportedEncodingException Thrown when cannot generate code.
     * @throws InternalErrorException Thrown when cannot find Facility for given ID.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
     */
    Long createMoveToProductionRequest(Long facilityId, User user, List<String> authorities)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException, PerunUnknownException, PerunConnectionException;

    /**
     * Update existing request in DB with new data.
     * @param requestId ID of request in DB.
     * @param userId ID of requesting user.
     * @param attributes Attributes set for SP (key = attribute name, value = attribute)
     * @return True if everything went OK.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when request could not be updated.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL, when param
     * "attributes" is NULL.
     *
     */
    boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException;

    /**
     * Get detailed request.
     * @param requestId ID of request.
     * @param userId ID of user.
     * @return Found request.
     * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
     * @throws InternalErrorException Thrown when cannot find request for given ID.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL.
     */
    Request getRequest(Long requestId, Long userId) throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException;

    /**
     * Get all requests user can access (is requester or admin(manager) of facility)
     * @param userId ID of user.
     * @return List of requests.
     * @throws IllegalArgumentException Thrown when param "userId" is NULL.
     */
    List<Request> getAllUserRequests(Long userId) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get all requests stored in system.
     * @param adminId ID of admin.
     * @return List of found requests.
     * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
     * @throws IllegalArgumentException Thrown when param "adminId" is NULL.
     */
    List<Request> getAllRequests(Long adminId) throws UnauthorizedActionException;

    /**
     * Get details of facility for the signatures interface
     * @param code code
     * @return Fetched request object.
     * @throws BadPaddingException Thrown when cannot decode code.
     * @throws InvalidKeyException Thrown when cannot decode code.
     * @throws IllegalBlockSizeException Thrown when cannot decode code.
     * @throws ExpiredCodeException Thrown when code is expired.
     * @throws InternalErrorException Thrown when request cannot be found in DB.
     * @throws IllegalArgumentException Thrown when param "code" is NULL or empty.
     */
    Request getRequestForSignatureByCode(String code)
            throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ExpiredCodeException,
            InternalErrorException;

    /**
     * Approve request.
     * @param requestId ID of request.
     * @param userId ID of user (ADMIN) approving the request.
     * @return TRUE if everything went OK, FALSE otherwise
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws CannotChangeStatusException when status of the request cannot be changed.
     * @throws InternalErrorException Thrown when cannot find request for given ID.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL.
     *
     */
    boolean approveRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException;

    /**
     * Reject request.
     * @param requestId ID of request.
     * @param userId ID of user (ADMIN) rejecting the request.
     * @return TRUE if everything went OK, FALSE otherwise
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws CannotChangeStatusException when status of the request cannot be changed.
     * @throws InternalErrorException Thrown when cannot find request for given ID.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL.
     */
    boolean rejectRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

    /**
     * Ask requester to make changes to the request.
     * @param requestId ID of request.
     * @param userId ID of user (ADMIN) asking for changes.
     * @param attributes Map (key = attribute name, value = PerunAttribute) of attributes.
     *                   It contains comments left by ADMIN.
     * @return TRUE if everything went OK, FALSE otherwise
     * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
     * @throws CannotChangeStatusException Thrown when status of the request cannot be changed.
     * @throws InternalErrorException Thrown when cannot find request for given ID.
     * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL.
     */
    boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;
}
