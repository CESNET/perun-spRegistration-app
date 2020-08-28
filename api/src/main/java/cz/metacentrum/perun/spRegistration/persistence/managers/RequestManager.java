package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.Request;

import java.util.List;
import java.util.Set;

/**
 * Interface for working with the Request object in the database.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface RequestManager {

	/**
	 * Create request in DB.
	 * @param request Request object to be stored.
	 * @return Generated ID of stored Request.
	 * @throws InternalErrorException Thrown when zero or more than one requests were created in DB.
	 * @throws IllegalArgumentException Thrown when param "request" is NULL.
	 */
	Long createRequest(Request request) throws InternalErrorException, ActiveRequestExistsException;

	/**
	 * Update request in DB.
	 * @param request Request object with updated data.
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws InternalErrorException Thrown when zero or more than one requests were updated in DB.
	 * @throws IllegalArgumentException Thrown when param "request" is NULL.
	 */
	boolean updateRequest(Request request) throws InternalErrorException;

	/**
	 * Delete request from DB.
	 * @param reqId ID of Request to be deleted.
	 * @return TRUE if everything went OK, FALSE otherwise.
	 * @throws InternalErrorException Thrown when zero or more than one requests were deleted in DB.
	 * @throws IllegalArgumentException Thrown when param "reqId" is NULL.
	 */
	boolean deleteRequest(Long reqId) throws InternalErrorException;

	/**
	 * Get request specified by ID.
	 * @param reqId ID of request.
	 * @return Found Request object.
	 * @throws IllegalArgumentException Thrown when param "reqId" is NULL.
	 */
	Request getRequestById(Long reqId);

	/**
	 * Get all requests from DB.
	 * @return List of found Request objects.
	 */
	List<Request> getAllRequests();

	/**
	 * Get all requests from DB where specified user is a requester.
	 * @param userId ID of user.
	 * @return List of found Request objects.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	List<Request> getAllRequestsByUserId(Long userId);

	/**
	 * Get all requests from DB with specified status.
	 * @param status Status of Requests.
	 * @return List of found Request objects.
	 * @throws IllegalArgumentException Thrown when param "status" is NULL.
	 */
	List<Request> getAllRequestsByStatus(RequestStatus status);

	/**
	 * Get all requests from DB with specified action.
	 * @param action Action of Request.
	 * @return List of found Request objects.
	 * @throws IllegalArgumentException Thrown when param "action" is NULL.
	 */
	List<Request> getAllRequestsByAction(RequestAction action);

	/**
	 * Get all requests from DB associated with facility specified by ID.
	 * @param facilityId ID of associated facility.
	 * @return List of found Request objects.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	List<Request> getAllRequestsByFacilityId(Long facilityId);

	/**
	 * Get all requests from DB associated with facilities specified by IDs.
	 * @param facilityIds IDs of associated facilities.
	 * @return List of found Request objects.
	 * @throws IllegalArgumentException Thrown when param "facilityIds" is NULL or empty.
	 */
	List<Request> getAllRequestsByFacilityIds(Set<Long> facilityIds);

	/**
	 * Get id of active request for Facility.
	 * @param facilityId Id of Facility.
	 * @return Id of found request (request with status different than APPROVED/REJECTED), NULL otherwise
	 * @throws InternalErrorException Thrown when more than ONE active request found for Facility
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	Long getActiveRequestIdByFacilityId(Long facilityId) throws InternalErrorException;

}
