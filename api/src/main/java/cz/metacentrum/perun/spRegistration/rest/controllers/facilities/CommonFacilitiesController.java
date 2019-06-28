package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;


import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
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
 * Controller handling common actions related to Facilities.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class CommonFacilitiesController {

	private static final Logger log = LoggerFactory.getLogger(CommonFacilitiesController.class);

	private final UserCommandsService service;

	@Autowired
	public CommonFacilitiesController(UserCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/facility/{facilityId}")
	public Facility facilityDetail(@SessionAttribute("user") User user,
								   @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException
	{
		log.trace("facilityDetail(user(): {}, facilityId: {})", user.getId(), facilityId);

		Facility facility = service.getDetailedFacility(facilityId, user.getId());

		log.trace("facilityDetail() returns: {}", facility);
		return facility;
	}
}
