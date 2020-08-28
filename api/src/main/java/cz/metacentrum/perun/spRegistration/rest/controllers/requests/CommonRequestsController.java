package cz.metacentrum.perun.spRegistration.rest.controllers.requests;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * Controller handling common actions related to Requests.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class CommonRequestsController {

	private final RequestsService requestsService;

	@Autowired
	public CommonRequestsController(RequestsService requestsService) {
		this.requestsService = requestsService;
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
}
