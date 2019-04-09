package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;

import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.AdminCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.SpRegistrationApiException;
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

@RestController
public class AdminFacilitiesController {

	private static final Logger log = LoggerFactory.getLogger(AdminFacilitiesController.class);

	private final AdminCommandsService service;

	@Autowired
	public AdminFacilitiesController(AdminCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/allFacilities")
	public List<Facility> allFacilities(@SessionAttribute("user") User user) throws SpRegistrationApiException {
		log.debug("allFacilities({})", user.getId());
		try {
			return service.getAllFacilities(user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}
}
