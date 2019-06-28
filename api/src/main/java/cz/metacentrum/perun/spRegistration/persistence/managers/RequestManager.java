package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

/**
 * Interface for working with the Request object in the database.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface RequestManager {

	/**
	 * Set JDBC template
	 * @param template JDBC Template
	 * @throws IllegalArgumentException Thrown when param "template" is NULL, or it does not contain dataSource.
	 */
	void setJdbcTemplate(JdbcTemplate template);

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

	/**
	 * Checks if code is valid, meaning it is stored in DB. Unused codes are stored, if the code has been used it is removed.
	 * @param code code to be validated
	 * @return TRUE if valid, FALSE otherwise
	 * @throws IllegalArgumentException Thrown when param "code" is NULL or empty.
	 */
	boolean validateCode(String code);

	/**
	 * Store generated codes for signatures
	 * @param codes List of codes to be stored
	 * @return Number of codes stored.
	 * @throws InternalErrorException Thrown when inserting codes fails.
	 * @throws IllegalArgumentException Thrown when param "codes" is NULL or empty.
	 */
	int storeCodes(List<String> codes) throws InternalErrorException;

	/**
	 * Delete used code.
	 * @param code Code to be deleted.
	 * @return TRUE if code deleted, FALSE otherwise.
	 * @throws InternalErrorException Thrown when more than one code has been deleted.
	 * @throws IllegalArgumentException Thrown when param "code" is NULL or empty.
	 */
	boolean deleteUsedCode(String code) throws InternalErrorException;
}
