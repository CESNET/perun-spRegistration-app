package cz.metacentrum.perun.spRegistration.rest.controllers.requests;

import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Controller handling ADMIN actions related to Requests.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class AdminRequestsController {
	
	private final RequestsService requestsService;

	@Autowired
	public AdminRequestsController(RequestsService requestsService) {
		this.requestsService = requestsService;
	}

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
