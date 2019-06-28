package cz.metacentrum.perun.spRegistration.rest.controllers.requests;

import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CommonRequestsController {

	private static final Logger log = LoggerFactory.getLogger(CommonRequestsController.class);

	private final UserCommandsService service;

	@Autowired
	public CommonRequestsController(UserCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/request/{requestId}")
	public Request requestDetail(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId)
			throws InternalErrorException, UnauthorizedActionException
	{
		log.trace("requestDetail(user: {}, requestId: {})", user.getId(), requestId);

		Request request = service.getDetailedRequest(requestId, user.getId());

		log.trace("requestDetail() returns: {}", request);
		return request;
	}
}
