package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;

import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.AddAdminsService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
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
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling USER actions related to Facilities.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class UserFacilitiesController {

	private static final Logger log = LoggerFactory.getLogger(UserFacilitiesController.class);

	private final FacilitiesService facilitiesService;
	private final AddAdminsService addAdminsService;

	@Autowired
	public UserFacilitiesController(FacilitiesService facilitiesService, AddAdminsService addAdminsService) {
		this.addAdminsService = addAdminsService;
		this.facilitiesService = facilitiesService;
	}

	@GetMapping(path = "/api/userFacilities")
	public List<ProvidedService> userFacilities(@SessionAttribute("user") User user) throws ConnectorException	{
		log.trace("userFacilities({})", user.getId());

		List<ProvidedService> facilityList = facilitiesService.getAllUserFacilities(user.getId());

		log.trace("userFacilities() returns: {}", facilityList);
		return facilityList;
	}

	@PostMapping(path = "/api/addAdmins/{facilityId}")
	public boolean addAdmins(@SessionAttribute("user") User user,
							 @PathVariable("facilityId") Long facilityId,
							 @RequestBody List<String> adminEmails)
			throws BadPaddingException, InvalidKeyException, ConnectorException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, UnauthorizedActionException
	{
		log.trace("addAdminsNotify(user: {}, facilityId: {}, adminEmails: {})", user.getId(), facilityId, adminEmails);

		boolean successful = addAdminsService.addAdminsNotify(user, facilityId, adminEmails);

		log.trace("addAdmins() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/confirm")
	public boolean addAdminConfirm(@SessionAttribute("user") User user,
								   @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, ConnectorException, InternalErrorException, CodeNotStoredException {
		log.trace("addAdminConfirm(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		boolean successful = addAdminsService.confirmAddAdmin(user, code);

		log.trace("addAdminConfirm() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/reject")
	public void addAdminReject(@SessionAttribute("user") User user,
								   @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, InternalErrorException, CodeNotStoredException {
		log.trace("addAdminReject(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		addAdminsService.rejectAddAdmin(user, code);

		log.trace("addAdminReject() returns");
	}

	@GetMapping(path = "/api/addAdmin/getDetails/{hash}")
	public LinkCode addAdminGetInfo(@SessionAttribute("user") User user,
									@PathVariable("hash") String hash) {
		log.trace("addAdminGetInfo({}, {})", user, hash);

		LinkCode details = addAdminsService.getDetails(hash);

		log.trace("addAdminGetInfo({}, {}) returns: {}", user, hash, details);
		return details;
	}

	@GetMapping(path = "/api/addAdmin/getFacilityDetails/{facilityId}")
	public Facility addAdminGetFacilityDetail(@SessionAttribute("user") User user,
											  @PathVariable("facilityId") Long facilityId)
			throws BadPaddingException, ConnectorException, IllegalBlockSizeException, InternalErrorException,
			InvalidKeyException, UnauthorizedActionException
	{
		log.trace("addAdminGetFacilityDetail({}, {})", user, facilityId);

		Facility facility = addAdminsService.getFacilityDetails(facilityId, user);

		log.trace("addAdminGetFacilityDetail({}, {}) returns: {}", user, facility, facility);
		return facility;
	}

	@GetMapping(path = "/api/facilityWithInputs/{facilityId}")
	public Facility getDetailedFacilityWithInputs(@SessionAttribute("user") User user,
												  @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
		log.trace("getDetailedFacilityWithInputs(user: {}, facilityId: {})", user, facilityId);
		Facility facility = facilitiesService.getFacilityWithInputs(facilityId, user.getId());

		log.trace("getDetailedFacilityWithInputs() returns: {}", facility);
		return facility;
	}

}
