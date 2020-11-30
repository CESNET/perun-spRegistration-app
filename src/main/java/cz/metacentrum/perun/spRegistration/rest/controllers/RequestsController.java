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
import cz.metacentrum.perun.spRegistration.service.UtilsService;
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
	@NonNull private final UtilsService utilsService;

	@Autowired
	public RequestsController(@NonNull RequestsService requestsService, @NonNull UtilsService utilsService) {
		this.requestsService = requestsService;
		this.utilsService = utilsService;
	}

	@GetMapping(path = "/api/userRequests")
	public List<Request> userRequests(@NonNull @SessionAttribute("user") User user)
			throws PerunUnknownException, PerunConnectionException
	{
		return requestsService.getAllUserRequests(user.getId());
	}

	@PostMapping(path = "/api/register")
	public Long createRegistrationRequest(@NonNull @SessionAttribute("user") User user,
										  @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException
	{
		return requestsService.createRegistrationRequest(user.getId(), attributes);
	}

	@PostMapping(path = "/api/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@NonNull @SessionAttribute("user") User user,
											 @NonNull @RequestBody List<PerunAttribute> attributes,
											 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.createFacilityChangesRequest(facilityId, user.getId(), attributes);
	}

	@PostMapping(path = "/api/remove/{facilityId}")
	public Long createRemovalRequest(@NonNull @SessionAttribute("user") User user,
									 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.createRemovalRequest(user.getId(), facilityId);
	}

	@PostMapping(path = "/api/update/{requestId}")
	public boolean updateRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException, UnauthorizedActionException
	{
		// auth done at the service level, cannot do it here
		return requestsService.updateRequest(requestId, user.getId(), attributes);
	}

	@GetMapping(path = "/api/request/{requestId}")
	public Request requestDetail(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		// auth done at the service level, cannot do it here
		return requestsService.getRequest(requestId, user.getId());
	}

	@PostMapping(path = "/api/cancel/{requestId}")
	public boolean cancelRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		// auth done at the service level, cannot do it here
		return requestsService.cancelRequest(requestId, user.getId());
	}

	// admin

	@GetMapping(path = "/api/allRequests")
	public List<Request> allRequests(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.getAllRequests(user.getId());
	}

	@PostMapping(path = "/api/approve/{requestId}")
	public boolean approveRequest(@NonNull @SessionAttribute("user") User user,
								  @NonNull @PathVariable("requestId") Long requestId)
			throws CannotChangeStatusException, InternalErrorException, UnauthorizedActionException,
			BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
			PerunConnectionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.approveRequest(requestId, user.getId());
	}

	@PostMapping(path = "/api/reject/{requestId}")
	public boolean rejectRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.rejectRequest(requestId, user.getId());
	}

	@PostMapping(path = "/api/askForChanges/{requestId}")
	public boolean askForChanges(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.askForChanges(requestId, user.getId(), attributes);
	}

	@GetMapping(path = "/api/audit/getAllAuditLogs")
	public List<AuditLog> allAuditLogs(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.getAllAuditLogs(user.getId());
	}

	@GetMapping(path = "/api/audit/getLogsByReqId/{reqId}")
	public List<AuditLog> auditLogsByReqId(@NonNull @SessionAttribute("user") User user,
										   @NonNull @PathVariable("reqId") Long reqId)
			throws UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.getAuditLogsByReqId(reqId, user.getId());
	}

	@GetMapping(path = "/api/audit/getAuditLogById/{auditLogId}")
	public AuditLog auditLogDetail(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("auditLogId") Long auditLogId)
			throws InternalErrorException, UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.getAuditLog(auditLogId, user.getId());
	}

	@GetMapping(path = "/api/audit/getAuditLogsByService/{facilityId}")
	public List<AuditLog> auditLogsByService(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException {
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.getAuditLogsByService(facilityId, user.getId());
	}

}
