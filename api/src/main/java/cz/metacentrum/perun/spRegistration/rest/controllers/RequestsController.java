package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
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

	private final RequestsService requestsService;

	@Autowired
	public RequestsController(RequestsService requestsService) {
		this.requestsService = requestsService;
	}

	@GetMapping(path = "/api/userRequests")
	public List<Request> userRequests(@SessionAttribute("user") User user) throws PerunUnknownException, PerunConnectionException {
		log.trace("userRequests({})", user.getId());

		List<Request> requestList = requestsService.getAllUserRequests(user.getId());

		log.trace("userRequests() returns: {}", requestList);
		return requestList;
	}

	@PostMapping(path = "/api/register")
	public Long createRegistrationRequest(@SessionAttribute("user") User user,
										  @RequestBody List<PerunAttribute> attributes) throws InternalErrorException
	{
		log.trace("createRegistrationRequest(user: {}, attributes: {})", user.getId(), attributes);

		Long generatedId = requestsService.createRegistrationRequest(user.getId(), attributes);

		log.trace("createRegistrationRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@SessionAttribute("user") User user,
											 @RequestBody List<PerunAttribute> attributes,
											 @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException {
		log.trace("createFacilityChangesRequest(user: {}, facilityId: {}, attributes: {})", user.getId(),
				facilityId, attributes);

		Long generatedId = requestsService.createFacilityChangesRequest(facilityId, user.getId(), attributes);

		log.trace("createFacilityChangesRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/remove/{facilityId}")
	public Long createRemovalRequest(@SessionAttribute("user") User user,
									 @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException {
		log.trace("createRemovalRequest(user: {}, facilityId: {})", user.getId(), facilityId);

		Long generatedId = requestsService.createRemovalRequest(user.getId(), facilityId);

		log.trace("createRemovalRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/update/{requestId}")
	public boolean updateRequest(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId,
								 @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException, UnauthorizedActionException
	{
		log.trace("updateRequest(user: {}, requestId: {}, attributes: {})", user.getId(), requestId, attributes);

		boolean successful = requestsService.updateRequest(requestId, user.getId(), attributes);

		log.trace("updateRequest() returns: {}", successful);
		return successful;
	}

	@GetMapping(path = "/api/request/{requestId}")
	public Request requestDetail(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId)
			throws InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException {
		log.trace("requestDetail(user: {}, requestId: {})", user.getId(), requestId);

		Request request = requestsService.getRequest(requestId, user.getId());

		log.trace("requestDetail() returns: {}", request);
		return request;
	}

	// admin

	@GetMapping(path = "/api/allRequests")
	public List<Request> allRequests(@SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		log.trace("allRequests({})", user.getId());

		List<Request> requestList = requestsService.getAllRequests(user.getId());

		log.trace("allRequests() returns: {}", requestList);
		return requestList;
	}

	@PostMapping(path = "/api/approve/{requestId}")
	public boolean approveRequest(@SessionAttribute("user") User user,
								  @PathVariable("requestId") Long requestId)
			throws CannotChangeStatusException, InternalErrorException, UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException {
		log.trace("approveRequest(user: {}, requestId: {})", user.getId(), requestId);

		boolean successful = requestsService.approveRequest(requestId, user.getId());

		log.trace("approveRequest() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/reject/{requestId}")
	public boolean rejectRequest(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		log.trace("rejectRequest(user: {}, requestId: {})", user.getId(), requestId);

		boolean successful = requestsService.rejectRequest(requestId, user.getId());

		log.trace("rejectRequest() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/askForChanges/{requestId}")
	public boolean askForChanges(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId,
								 @RequestBody List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		log.trace("askForChanges(user: {}, requestId: {}, attributes: {})", user.getId(), requestId, attributes);

		boolean successful = requestsService.askForChanges(requestId, user.getId(), attributes);

		log.trace("askForChanges() returns: {}", successful);
		return successful;
	}
}
