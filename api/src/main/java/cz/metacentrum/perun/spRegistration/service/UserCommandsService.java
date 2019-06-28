package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.MalformedCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Service layer with methods specific for Users.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface UserCommandsService {

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
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws InternalErrorException Thrown when cannot fetch facility with given ID,
	 * when request could not be created or was created correctly.
	 * @throws ActiveRequestExistsException Thrown when there already exists active request for given facility.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL, when param
	 * "attributes" is NULL.
	 */
	Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException;

	/**
	 * Create request for removal of SP (which already exists as facility in Perun).
	 * @param userId ID of requesting user.
	 * @param facilityId ID of facility in Perun.
	 * @return Generated request ID after storing to the DB.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws InternalErrorException Thrown when cannot fetch facility with given ID,
	 * when request could not be created or was created correctly.
	 * @throws ActiveRequestExistsException Thrown when there already exists active request for given facility.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
	 */
	Long createRemovalRequest(Long userId, Long facilityId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException;

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
	 * Ask for moving the service to the production environment.
	 * @param facilityId ID of facility in Perun.
	 * @param userId ID of requesting user.
	 * @param authorities List to whom the emails should be sent
	 * @return Id of created request
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws BadPaddingException Thrown when cannot generate code.
	 * @throws InvalidKeyException Thrown when cannot generate code.
	 * @throws IllegalBlockSizeException Thrown when cannot generate code.
	 * @throws UnsupportedEncodingException Thrown when cannot generate code.
	 * @throws InternalErrorException Thrown when cannot find Facility for given ID.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
	 */
	Long requestMoveToProduction(Long facilityId, Long userId, List<String> authorities)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException,
			ActiveRequestExistsException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
			UnsupportedEncodingException;

	/**
	 * Get details of facility for the signatures interface
	 * @param code code
	 * @return Fetched request object.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws BadPaddingException Thrown when cannot decode code.
	 * @throws InvalidKeyException Thrown when cannot decode code.
	 * @throws IllegalBlockSizeException Thrown when cannot decode code.
	 * @throws MalformedCodeException Thrown when cannot decode code.
	 * @throws ExpiredCodeException Thrown when code is expired.
	 * @throws InternalErrorException Thrown when request cannot be found in DB.
	 * @throws IllegalArgumentException Thrown when param "code" is NULL or empty.
	 */
	Request getRequestDetailsForSignature(String code)
			throws ConnectorException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
			MalformedCodeException, ExpiredCodeException, InternalErrorException;

	/**
	 * Add signature for transfer to production
	 * @param user user signing the request
	 * @param code hash of request
	 * @param approved TRUE if approved, FALSE if rejected
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws BadPaddingException Thrown when cannot decode code.
	 * @throws InvalidKeyException Thrown when cannot decode code.
	 * @throws IllegalBlockSizeException Thrown when cannot decode code.
	 * @throws MalformedCodeException Thrown when cannot decode code.
	 * @throws InternalErrorException Thrown when request was not signed in DB.
	 * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
	 */
	boolean signTransferToProduction(User user, String code, boolean approved)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, InternalErrorException;

	/**
	 * Get approvals for request to transfer to production.
	 * @param requestId ID of request.
	 * @param userId ID of user displaying the approvals.
	 * @return List of approvals.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws InternalErrorException Thrown when request cannot be found in DB.
	 * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when "param" userId is NULL.
	 */
	List<RequestSignature> getApprovalsOfProductionTransfer(Long requestId, Long userId)
			throws UnauthorizedActionException, InternalErrorException;

	/**
	 * Get all facilities from Perun where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of facilities (empty or filled).
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException;

	/**
	 * Get all requests user can access (is requester or admin(manager) of facility)
	 * @param userId ID of user.
	 * @return List of requests.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	List<Request> getAllRequestsUserCanAccess(Long userId) throws ConnectorException;

	/**
	 * Get detailed request.
	 * @param requestId ID of request.
	 * @param userId ID of user.
	 * @return Found request.
	 * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
	 * @throws InternalErrorException Thrown when cannot find request for given ID.
	 * @throws IllegalArgumentException Thrown when param "requestId" is NULL, when param "userId" is NULL.
	 */
	Request getDetailedRequest(Long requestId, Long userId) throws UnauthorizedActionException, InternalErrorException;

	/**
	 * Get detailed facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return Found facility.
	 * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
	 */
	Facility getDetailedFacility(Long facilityId, Long userId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException;

	/**
	 * Get detailed facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return Found facility.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
	 */
	Facility getDetailedFacilityWithInputs(Long facilityId, Long userId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException;

	/**
	 * Add users as admins (managers) for facility in Perun.
	 * @param user User performing the action
	 * @param facilityId ID of facility in Perun.
	 * @param admins List of emails to whom the notification should be sent
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws BadPaddingException Thrown when cannot generate code.
	 * @throws InvalidKeyException Thrown when cannot generate code.
	 * @throws IllegalBlockSizeException Thrown when cannot generate code.
	 * @throws UnsupportedEncodingException Thrown when cannot generate code.
	 * @throws InternalErrorException Thrown when cannot find Facility for given ID.
	 * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "facilityId" is NULL, when param
	 * "admins" is NULL or empty.
	 */
	boolean addAdminsNotify(User user, Long facilityId, List<String> admins)
			throws UnauthorizedActionException, ConnectorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, UnsupportedEncodingException, InternalErrorException;

	/**
	 * Confirm request to be added as a facility admin.
	 * @param user user to be added or removed from facility admins.
	 * @param code code generated for the approval
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws BadPaddingException Thrown when cannot decrypt code.
	 * @throws InvalidKeyException Thrown when cannot decrypt code.
	 * @throws IllegalBlockSizeException Thrown when cannot decrypt code.
	 * @throws MalformedCodeException Thrown when cannot decrypt code.
	 * @throws ExpiredCodeException Thrown when used code is expired.
	 * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
	 */
	boolean confirmAddAdmin(User user, String code)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, ConnectorException, InternalErrorException;

	/**
	 * Reject request to be added as a facility admin.
	 * @param user user to be added or removed from facility admins.
	 * @param code code generated for the approval
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws BadPaddingException Thrown when cannot decrypt code.
	 * @throws InvalidKeyException Thrown when cannot decrypt code.
	 * @throws IllegalBlockSizeException Thrown when cannot decrypt code.
	 * @throws MalformedCodeException Thrown when cannot decrypt code.
	 * @throws ExpiredCodeException Thrown when used code is expired.
	 * @throws IllegalArgumentException Thrown when param "user" is NULL, when param "code" is NULL or empty.
	 */
	boolean rejectAddAdmin(User user, String code) throws IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, MalformedCodeException, ExpiredCodeException, InternalErrorException;

	/**
	 * Validate code for signature
	 * @param code code to be validated
	 * @return True if valid, false otherwise
	 */
	boolean validateCode(String code) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException;
}
