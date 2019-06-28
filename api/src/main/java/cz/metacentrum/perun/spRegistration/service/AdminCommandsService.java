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
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface AdminCommandsService {

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
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, ConnectorException;

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

	/**
	 * Get all requests stored in system.
	 * @param adminId ID of admin.
	 * @return List of found requests.
	 * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
	 * @throws IllegalArgumentException Thrown when param "adminId" is NULL.
	 */
	List<Request> getAllRequests(Long adminId) throws UnauthorizedActionException;

	/**
	 * Get all facilities stored in Perun.
	 * @param adminId ID of admin.
	 * @return List of found facilities.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "adminId" is NULL.
	 */
	List<Facility> getAllFacilities(Long adminId) throws UnauthorizedActionException, ConnectorException;

}
