package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;

import java.util.List;

/**
 * Service layer with methods specific for Admins.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface AdminService {

	/**
	 * Approve request.
	 * @param requestId ID of request.
	 * @param userId ID of user (ADMIN) approving the request.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean approveRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, RPCException;

	/**
	 * Reject request.
	 * @param requestId ID of request.
	 * @param userId ID of user (ADMIN) rejecting the request.
	 * @param message Message explaining why the request has been rejected.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean rejectRequest(Long requestId, Long userId, String message)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

	/**
	 * Ask requester to make changes to the request.
	 * @param requestId ID of request.
	 * @param userId ID of user (ADMIN) asking for changes.
	 * @param attributes Map (key = attribute name, value = PerunAttribute) of attributes.
	 *                   It contains comments left by ADMIN.
	 * @return True if everyting went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

	/**
	 * Mark request of transfer to the production environment as approved.
	 * @param requestId id of request for transfer
	 * @param userId ID of user (ADMIN) approving the request.
	 * @return True if everything went OK.
	 */
	boolean approveTransferToProduction(Long requestId, Long userId) throws InternalErrorException, UnauthorizedActionException, RPCException;

	/**
	 * Get approvals for request to transfer to production.
	 * @param requestId ID of request.
	 * @param userId ID of user (ADMIN) displaying the approvals.
	 * @return List of approvals.
	 */
	List<RequestSignature> getApprovalsOfProductionTransfer(Long requestId, Long userId) throws UnauthorizedActionException;

	/**
	 * Get all requests stored in system.
	 * @param adminId ID of admin.
	 * @return List of found requests.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	List<Request> getAllRequests(Long adminId) throws UnauthorizedActionException;

	/**
	 * Get all facilities stored in Perun.
	 * @param adminId ID of admin.
	 * @return List of found facilities.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	List<Facility> getAllFacilities(Long adminId) throws UnauthorizedActionException, RPCException;

	/**
	 * Add users as admins (managers) for facility in Perun
	 * @param userId ID of admin performing the action.
	 * @param facilityId ID of facility in Perun.
	 * @param admins List of IDs of new facility admins.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	boolean addAdmins(Long userId, Long facilityId, List<Long> admins) throws UnauthorizedActionException, RPCException;

	/**
	 * Remove users from admins (managers) of facility in Perun.
	 * @param userId ID of admin performing the action.
	 * @param facilityId ID of facility in Perun.
	 * @param admins List of IDs of admins to be removed.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	boolean removeAdmins(Long userId, Long facilityId, List<Long> admins) throws UnauthorizedActionException, RPCException;

}
