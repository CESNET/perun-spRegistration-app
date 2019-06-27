package cz.metacentrum.perun.spRegistration.rest.controllers.facilities;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.MalformedCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling USER actions related to Facilities.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
@RestController
public class UserFacilitiesController {

	private static final Logger log = LoggerFactory.getLogger(UserFacilitiesController.class);

	private final UserCommandsService service;

	public UserFacilitiesController(UserCommandsService service) {
		this.service = service;
	}

	@GetMapping(path = "/api/userFacilities")
	public List<Facility> userFacilities(@SessionAttribute("user") User user) throws ConnectorException	{
		log.trace("userFacilities({})", user.getId());

		List<Facility> facilityList = service.getAllFacilitiesWhereUserIsAdmin(user.getId());

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

		boolean successful = service.addAdminsNotify(user, facilityId, adminEmails);

		log.trace("addAdmins() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/confirm")
	public boolean addAdminConfirm(@SessionAttribute("user") User user,
								   @RequestBody String code)
			throws UnsupportedEncodingException, BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InvalidKeyException, ConnectorException, InternalErrorException
	{
		log.trace("addAdminConfirm(user: {}, code: {})", user, code);

		code = ApiUtils.decodeCode(code);
		boolean successful = service.confirmAddAdmin(user, code);

		log.trace("addAdminConfirm() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/reject")
	public boolean addAdminReject(@SessionAttribute("user") User user,
								   @RequestBody String code)
			throws UnsupportedEncodingException, BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InvalidKeyException, InternalErrorException
	{
		log.trace("addAdminReject(user: {}, code: {})", user, code);

		code = ApiUtils.decodeCode(code);
		boolean successful = service.rejectAddAdmin(user, code);

		log.trace("addAdminReject() returns: {}", successful);
		return successful;
	}

	@GetMapping(path = "/api/facilityWithInputs/{facilityId}")
	public Facility getDetailedFacilityWithInputs(@SessionAttribute("user") User user,
												  @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException
	{
		log.trace("getDetailedFacilityWithInputs(user: {}, facilityId: {})", user, facilityId);
		Facility facility = service.getDetailedFacilityWithInputs(facilityId, user.getId());

		log.trace("getDetailedFacilityWithInputs() returns: {}", facility);
		return facility;
	}

}
