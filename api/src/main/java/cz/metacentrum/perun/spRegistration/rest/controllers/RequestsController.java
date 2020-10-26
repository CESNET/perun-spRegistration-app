package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling actions related to Requests.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class RequestsController {

	@NonNull private final RequestsService requestsService;

	@Autowired
	public RequestsController(@NonNull RequestsService requestsService) {
		this.requestsService = requestsService;
	}

	@GetMapping(path = "/api/userRequests")
	public List<Request> userRequests(@NonNull @SessionAttribute("user") User user)
			throws PerunUnknownException, PerunConnectionException
	{
		log.trace("userRequests({})", user.getId());

		List<Request> requestList = requestsService.getAllUserRequests(user.getId());

		log.trace("userRequests() returns: {}", requestList);
		return requestList;
	}

	@PostMapping(path = "/api/register")
	public Long createRegistrationRequest(@NonNull @SessionAttribute("user") User user,
										  @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException
	{
		log.trace("createRegistrationRequest(user: {}, attributes: {})", user.getId(), attributes);

		Long generatedId = requestsService.createRegistrationRequest(user.getId(), attributes);

		log.trace("createRegistrationRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@NonNull @SessionAttribute("user") User user,
											 @NonNull @RequestBody List<PerunAttribute> attributes,
											 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		log.trace("createFacilityChangesRequest(user: {}, facilityId: {}, attributes: {})", user.getId(),
				facilityId, attributes);

		Long generatedId = requestsService.createFacilityChangesRequest(facilityId, user.getId(), attributes);

		log.trace("createFacilityChangesRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/remove/{facilityId}")
	public Long createRemovalRequest(@NonNull @SessionAttribute("user") User user,
									 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		log.trace("createRemovalRequest(user: {}, facilityId: {})", user.getId(), facilityId);

		Long generatedId = requestsService.createRemovalRequest(user.getId(), facilityId);

		log.trace("createRemovalRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/update/{requestId}")
	public boolean updateRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException, UnauthorizedActionException
	{
		log.trace("updateRequest(user: {}, requestId: {}, attributes: {})", user.getId(), requestId, attributes);

		boolean successful = requestsService.updateRequest(requestId, user.getId(), attributes);

		log.trace("updateRequest() returns: {}", successful);
		return successful;
	}

	@GetMapping(path = "/api/request/{requestId}")
	public Request requestDetail(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		log.trace("requestDetail(user: {}, requestId: {})", user.getId(), requestId);

		Request request = requestsService.getRequest(requestId, user.getId());

		log.trace("requestDetail() returns: {}", request);
		return request;
	}

	// admin

	@GetMapping(path = "/api/allRequests")
	public List<Request> allRequests(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		log.trace("allRequests({})", user.getId());

		List<Request> requestList = requestsService.getAllRequests(user.getId());

		log.trace("allRequests() returns: {}", requestList);
		return requestList;
	}

	@PostMapping(path = "/api/approve/{requestId}")
	public boolean approveRequest(@NonNull @SessionAttribute("user") User user,
								  @NonNull @PathVariable("requestId") Long requestId)
			throws CannotChangeStatusException, InternalErrorException, UnauthorizedActionException,
			BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
			PerunConnectionException
	{
		log.trace("approveRequest(user: {}, requestId: {})", user.getId(), requestId);

		boolean successful = requestsService.approveRequest(requestId, user.getId());

		log.trace("approveRequest() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/reject/{requestId}")
	public boolean rejectRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		log.trace("rejectRequest(user: {}, requestId: {})", user.getId(), requestId);

		boolean successful = requestsService.rejectRequest(requestId, user.getId());

		log.trace("rejectRequest() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/askForChanges/{requestId}")
	public boolean askForChanges(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		log.trace("askForChanges(user: {}, requestId: {}, attributes: {})", user.getId(), requestId, attributes);

		boolean successful = requestsService.askForChanges(requestId, user.getId(), attributes);

		log.trace("askForChanges() returns: {}", successful);
		return successful;
	}

	@GetMapping(path = "/api/audit/getAllAuditLogs")
	public List<AuditLog> allAuditLogs(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		log.trace("allAuditLogs({})", user.getId());

		List<AuditLog> auditLogsList = requestsService.getAllAuditLogs(user.getId());

		log.trace("allAuditLogs() returns: {}", auditLogsList);
		return auditLogsList;
	}

	@GetMapping(path = "/api/audit/getLogsByReqId/{reqId}")
	public List<AuditLog> auditLogsByReqId(@NonNull @SessionAttribute("user") User user,
										   @NonNull @PathVariable("reqId") Long reqId)
			throws UnauthorizedActionException
	{
		log.trace("auditLogsByReqId(user: {}, reqId: {})", user.getId(), reqId);

		List<AuditLog> auditLogsList = requestsService.getAuditLogsByReqId(reqId, user.getId());

		log.trace("auditLogsByReqId() returns: {}", auditLogsList);
		return auditLogsList;
	}

	@GetMapping(path = "/api/audit/getAuditLogById/{auditLogId}")
	public AuditLog auditLogDetail(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("auditLogId") Long auditLogId)
			throws InternalErrorException, UnauthorizedActionException
	{
		log.trace("auditLogDetail(user: {}, auditLogId: {})", user.getId(), auditLogId);

		AuditLog auditLog = requestsService.getAuditLog(auditLogId, user.getId());

		log.trace("auditLogDetail() returns: {}", auditLog);
		return auditLog;
	}

	@GetMapping(path = "/api/audit/getAuditLogsByService/{facilityId}")
	public List<AuditLog> auditLogsByService(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException
	{
		log.trace("auditLogsByService(user: {}, facilityId: {})", user.getId(), facilityId);

		List<AuditLog> auditLogs = requestsService.getAuditLogsByService(facilityId, user.getId());

		log.trace("auditLogsByService() returns: {}", auditLogs);
		return auditLogs;
	}

}
