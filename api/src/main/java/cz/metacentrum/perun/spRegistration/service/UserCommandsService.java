package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.CreateRequestException;
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
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
public interface UserCommandsService {

	/**
	 * Create request for Registration of SP (initial version of facility, not stored in Perun yet).
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute).
	 * @return Generated request ID after storing to the DB.
	 */
	Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException, CreateRequestException;

	/**
	 * Create request for changes of SP (which already exists as facility in Perun).
	 * @param facilityId ID of facility in Perun.
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute).
	 * @return Generated request ID after storing to the DB.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, CreateRequestException;

	/**
	 * Create request for removal of SP (which already exists as facility in Perun).
	 * @param userId ID of requesting user.
	 * @param facilityId ID of facility in Perun.
	 * @return Generated request ID after storing to the DB.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Long createRemovalRequest(Long userId, Long facilityId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, CreateRequestException;

	/**
	 * Update existing request in DB with new data.
	 * @param requestId ID of request in DB.
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute)
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
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
	 */
	Long requestMoveToProduction(Long facilityId, Long userId, List<String> authorities)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException, CreateRequestException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException;

	/**
	 * Get details of facility for the signatures interface
	 * @param code code
	 * @return Fetched request object
	 * @throws ConnectorException when some problem with Perun RPC has occurred.
	 */
	Request getRequestDetailsForSignature(String code) throws ConnectorException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, MalformedCodeException, ExpiredCodeException;

	/**
	 * Add signature for transfer to production
	 * @param user user signing the request
	 * @param code hash of request
	 * @return True if everything went OK
	 */
	boolean signTransferToProduction(User user, String code) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException, ExpiredCodeException, InternalErrorException;

	/**
	 * Get approvals for request to transfer to production.
	 * @param requestId ID of request.
	 * @param userId ID of user displaying the approvals.
	 * @return List of approvals.
	 */
	List<RequestSignature> getApprovalsOfProductionTransfer(Long requestId, Long userId) throws UnauthorizedActionException, InternalErrorException;

	/**
	 * Get all facilities from Perun where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of facilities.
	 */
	List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException;

	/**
	 * Get all requests user can access (is requester or admin(manager) of facility)
	 * @param userId ID of user.
	 * @return List of requests.
	 */
	List<Request> getAllRequestsUserCanAccess(Long userId) throws ConnectorException;

	/**
	 * Get detailed request.
	 * @param requestId ID of request.
	 * @param userId ID of user.
	 * @return Found request.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Request getDetailedRequest(Long requestId, Long userId) throws UnauthorizedActionException, InternalErrorException;

	/**
	 * Get detailed facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return Found facility.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Facility getDetailedFacility(Long facilityId, Long userId) throws UnauthorizedActionException, ConnectorException, InternalErrorException;

	/**
	 * Get detailed facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return Found facility.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Facility getDetailedFacilityWithInputs(Long facilityId, Long userId) throws UnauthorizedActionException, ConnectorException, InternalErrorException;

	/**
	 * Add users as admins (managers) for facility in Perun
	 * @param user user performing the action
	 * @param facilityId ID of facility in Perun.
	 * @param admins List of emails to whom the notification should be sent
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	boolean addAdminsNotify(User user, Long facilityId, List<String> admins) throws UnauthorizedActionException, ConnectorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException, InternalErrorException;

	/**
	 * Confirm request to be added or removed as a facility admin.
	 * @param user user to be added or removed from facility admins
	 * @param code code generated for the approval
	 * @return True if everything went ok
	 */
	boolean confirmAddAdmin(User user, String code) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException, ExpiredCodeException, ConnectorException;
}
