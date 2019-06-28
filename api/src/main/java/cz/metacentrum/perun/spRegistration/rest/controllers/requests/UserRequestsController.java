package cz.metacentrum.perun.spRegistration.rest.controllers.requests;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

/**
 * Controller handling USER actions related to Requests.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class UserRequestsController {

	private static final Logger log = LoggerFactory.getLogger(UserRequestsController.class);

	private final UserCommandsService service;

	@Autowired
	public UserRequestsController(UserCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/userRequests")
	public List<Request> userRequests(@SessionAttribute("user") User user) throws ConnectorException {
		log.trace("userRequests({})", user.getId());
		
		List<Request> requestList = service.getAllRequestsUserCanAccess(user.getId());

		log.trace("userRequests() returns: {}", requestList);
		return requestList;
	}

	@PostMapping(path = "/api/register")
	public Long createRegistrationRequest(@SessionAttribute("user") User user,
										  @RequestBody List<PerunAttribute> attributes) throws InternalErrorException
	{
		log.trace("createRegistrationRequest(user: {}, attributes: {})", user.getId(), attributes);
		
		Long generatedId = service.createRegistrationRequest(user.getId(), attributes);

		log.trace("createRegistrationRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@SessionAttribute("user") User user,
											 @RequestBody List<PerunAttribute> attributes,
											 @PathVariable("facilityId") Long facilityId)
			throws ConnectorException, ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException
	{
		log.trace("createFacilityChangesRequest(user: {}, facilityId: {}, attributes: {})", user.getId(),
				facilityId, attributes);
		
		Long generatedId = service.createFacilityChangesRequest(facilityId, user.getId(), attributes);

		log.trace("createFacilityChangesRequest() returns: {}", generatedId);
		return generatedId;
	}

	@PostMapping(path = "/api/remove/{facilityId}")
	public Long createRemovalRequest(@SessionAttribute("user") User user,
									 @PathVariable("facilityId") Long facilityId)
			throws ConnectorException, ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException
	{
		log.trace("createRemovalRequest(user: {}, facilityId: {})", user.getId(), facilityId);
		
		Long generatedId = service.createRemovalRequest(user.getId(), facilityId);

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
		
		boolean successful = service.updateRequest(requestId, user.getId(), attributes);

		log.trace("updateRequest() returns: {}", successful);
		return successful;
	}
}
