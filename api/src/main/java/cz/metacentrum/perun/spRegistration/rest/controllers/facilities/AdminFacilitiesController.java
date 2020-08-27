package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;

import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling ADMIN actions related to Facilities.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class AdminFacilitiesController {

	private final FacilitiesService facilitiesService;
	private final UtilsService utilsService;

	@Autowired
	public AdminFacilitiesController(FacilitiesService facilitiesService, UtilsService utilsService) {
		this.facilitiesService = facilitiesService;
		this.utilsService = utilsService;
	}

	@GetMapping(path = "/api/allFacilities")
	public List<ProvidedService> allFacilities(@SessionAttribute("user") User user)
			throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		log.trace("allFacilities({})", user.getId());

		List<ProvidedService> facilityList = facilitiesService.getAllFacilities(user.getId());

		log.trace("allFacilities() returns: {}", facilityList);
		return facilityList;
	}

	@PostMapping(path = "api/facility/regenerateClientSecret/{facilityId}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public PerunAttribute generateClientSecret(@SessionAttribute("user") User user,
											   @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException {
		log.trace("generateClientSecret(user: {}, facilityId: {})", user, facilityId);
		PerunAttribute clientSecret = utilsService.regenerateClientSecret(user.getId(), facilityId);

		log.trace("generateClientSecret() returns: {}", clientSecret);
		return clientSecret;
	}
}
