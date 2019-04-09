package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.CreateRequestException;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface for working with the Request in the database.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface RequestManager {

	void setJdbcTemplate(JdbcTemplate template);

	/**
	 * Create request in DB.
	 * @param request Request object to be stored.
	 * @return Generated ID of stored Request.
	 */
	Long createRequest(Request request) throws InternalErrorException, CreateRequestException;

	/**
	 * Update request in DB.
	 * @param request Request object with updated data.
	 * @return True if everything went OK.
	 */
	boolean updateRequest(Request request);

	/**
	 * Delete request from DB.
	 * @param reqId ID of Request to be deleted.
	 * @return True if everything went OK.
	 */
	boolean deleteRequest(Long reqId);

	/**
	 * Get request specified by ID.
	 * @param reqId ID of request.
	 * @return Found Request object.
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
	 */
	List<Request> getAllRequestsByUserId(Long userId);

	/**
	 * Get all requests from DB with specified status.
	 * @param status Status of Requests.
	 * @return List of found Request objects.
	 */
	List<Request> getAllRequestsByStatus(RequestStatus status);

	/**
	 * Get all requests from DB with specified action.
	 * @param action Action of Request.
	 * @return List of found Request objects.
	 */
	List<Request> getAllRequestsByAction(RequestAction action);

	/**
	 * Get all requests from DB associated with facility specified by ID.
	 * @param facilityId ID of associated facility.
	 * @return List of found Request objects.
	 */
	List<Request> getAllRequestsByFacilityId(Long facilityId);

	/**
	 * Get all requests from DB associated with facilities specified by IDs.
	 * @param facilityIds IDs of associated facilities.
	 * @return List of found Request objects.
	 */
	List<Request> getAllRequestsByFacilityIds(Set<Long> facilityIds);

	/**
	 * Get id of active request for Facility.
	 * @param facilityId Id of facility
	 * @return Id of found request (request with status different than APPROVED/REJECTED), null otherwise
	 */
	Long getActiveRequestIdByFacilityId(Long facilityId) throws InternalErrorException;

	/**
	 * Add signature for moving to production
	 * @param requestId id of request to be signed
	 * @param userId id of user giving the signature
	 * @param userName name of signing user
	 * @return True if everything went OK
	 */
	boolean addSignature(Long requestId, Long userId, String userName);

	/**
	 * Get all approvals for transferring of service into production environment
	 * @param requestId id of transfer request
	 * @return List of associated approvals
	 */
	List<RequestSignature> getRequestSignatures(Long requestId);
}
