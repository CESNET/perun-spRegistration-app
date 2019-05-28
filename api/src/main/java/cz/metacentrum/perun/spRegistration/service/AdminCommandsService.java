package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
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
public interface AdminCommandsService {

	/**
	 * Approve request.
	 * @param requestId ID of request.
	 * @param userId ID of user (ADMIN) approving the request.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean approveRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, ConnectorException;

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
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException;

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
	List<Facility> getAllFacilities(Long adminId) throws UnauthorizedActionException, ConnectorException;

}
