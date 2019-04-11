package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;

import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.SpRegistrationApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class UserFacilitiesController {

	private static final Logger log = LoggerFactory.getLogger(UserFacilitiesController.class);

	private final UserCommandsService service;

	public UserFacilitiesController(UserCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/userFacilities")
	public List<Facility> userFacilities(@SessionAttribute("user") User user) throws SpRegistrationApiException {
		log.debug("userFacilities({})", user.getId());
		try {
			return service.getAllFacilitiesWhereUserIsAdmin(user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@PostMapping(path = "/api/addAdmins/{facilityId}")
	public boolean addAdmins(@SessionAttribute("user") User user,
							 @PathVariable("facilityId") Long facilityId,
							 @RequestBody List<String> adminEmails) throws SpRegistrationApiException {
		log.debug("addAdminsNotify(user: {}, facilityId: {}, adminEmails: {})", user.getId(), facilityId, adminEmails);
		try {
			return service.addAdminsNotify(user, facilityId, adminEmails);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@PostMapping(path = "/api/addAdmin/confirm")
	public boolean addAdminConfirm(@SessionAttribute("user") User user,
								   @RequestBody String code) throws SpRegistrationApiException {
		log.debug("addAdminConfirm(user: {}, code: {})", user, code);
		try {
			if (code.startsWith("\"")) {
				code = code.substring(1, code.length() - 1);
			}
			code = URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
			return service.confirmAddAdmin(user, code);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@GetMapping(path = "/api/facilityWithInputs/{facilityId}")
	public Facility getDetailedFacilityWithInputs(@SessionAttribute("user") User user,
												  @PathVariable("facilityId") Long facilityId) throws SpRegistrationApiException {
		log.debug("getDetailedFacilityWithInputs(user: {}, facilityId: {})", user, facilityId);
		try {
			return service.getDetailedFacilityWithInputs(facilityId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

}
