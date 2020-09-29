package cz.metacentrum.perun.spRegistration.rest.controllers;


import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.AddAdminsService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
 * Controller handling actions related to Facilities.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class FacilitiesController {

	@NonNull private final FacilitiesService facilitiesService;
	@NonNull private final AddAdminsService addAdminsService;
	@NonNull private final UtilsService utilsService;

	@Autowired
	public FacilitiesController(@NonNull FacilitiesService facilitiesService,
								@NonNull AddAdminsService addAdminsService,
								@NonNull UtilsService utilsService)
	{
		this.addAdminsService = addAdminsService;
		this.facilitiesService = facilitiesService;
		this.utilsService = utilsService;
	}

	@GetMapping(path = "/api/facility/{facilityId}")
	public Facility facilityDetail(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException {
		log.trace("facilityDetail(user(): {}, facilityId: {})", user.getId(), facilityId);

		Facility facility = facilitiesService.getFacility(facilityId, user.getId(), true, true);

		log.trace("facilityDetail() returns: {}", facility);
		return facility;
	}

	@GetMapping(path = "/api/facility/signature/{facilityId}")
	public Facility facilityDetailSignature(@NonNull @SessionAttribute("user") User user,
											@NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		log.trace("facilityDetailSignature(user(): {}, facilityId: {})", user.getId(), facilityId);

		Facility facility = facilitiesService.getFacility(facilityId, user.getId(), false, false);
		facility.getAttributes().get(AttributeCategory.PROTOCOL).clear();
		facility.getAttributes().get(AttributeCategory.ACCESS_CONTROL).clear();

		log.trace("facilityDetailSignature() returns: {}", facility);
		return facility;
	}

	@GetMapping(path = "/api/userFacilities")
	public List<ProvidedService> userFacilities(@NonNull @SessionAttribute("user") User user)
			throws PerunUnknownException, PerunConnectionException
	{
		log.trace("userFacilities({})", user.getId());

		List<ProvidedService> facilityList = facilitiesService.getAllUserFacilities(user.getId());

		log.trace("userFacilities() returns: {}", facilityList);
		return facilityList;
	}

	@PostMapping(path = "/api/addAdmins/{facilityId}")
	public boolean addAdmins(@NonNull @SessionAttribute("user") User user,
							 @NonNull @PathVariable("facilityId") Long facilityId,
							 @NonNull @RequestBody List<String> adminEmails)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		log.trace("addAdminsNotify(user: {}, facilityId: {}, adminEmails: {})", user.getId(), facilityId, adminEmails);

		boolean successful = addAdminsService.addAdminsNotify(user, facilityId, adminEmails);

		log.trace("addAdmins() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/confirm")
	public boolean addAdminConfirm(@NonNull @SessionAttribute("user") User user,
								   @NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, InternalErrorException, CodeNotStoredException, PerunUnknownException,
			PerunConnectionException
	{
		log.trace("addAdminConfirm(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		boolean successful = addAdminsService.confirmAddAdmin(user, code);

		log.trace("addAdminConfirm() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/addAdmin/reject")
	public void addAdminReject(@NonNull @SessionAttribute("user") User user,
							   @NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, InternalErrorException, CodeNotStoredException {
		log.trace("addAdminReject(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		addAdminsService.rejectAddAdmin(user, code);

		log.trace("addAdminReject() returns");
	}

	@GetMapping(path = "/api/addAdmin/getDetails/{hash}")
	public LinkCode addAdminGetInfo(@NonNull @SessionAttribute("user") User user,
									@NonNull @PathVariable("hash") String hash) {
		log.trace("addAdminGetInfo({}, {})", user, hash);

		LinkCode details = addAdminsService.getCodeByString(hash);

		log.trace("addAdminGetInfo({}, {}) returns: {}", user, hash, details);
		return details;
	}

	@GetMapping(path = "/api/addAdmin/getFacilityDetails/{facilityId}")
	public Facility addAdminGetFacilityDetail(@NonNull @SessionAttribute("user") User user,
											  @NonNull @PathVariable("facilityId") Long facilityId)
			throws BadPaddingException, IllegalBlockSizeException, InternalErrorException,
			InvalidKeyException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		log.trace("addAdminGetFacilityDetail({}, {})", user, facilityId);

		Facility facility = addAdminsService.getFacilityDetails(facilityId, user);

		log.trace("addAdminGetFacilityDetail({}, {}) returns: {}", user, facility, facility);
		return facility;
	}

	@GetMapping(path = "/api/facilityWithInputs/{facilityId}")
	public Facility getDetailedFacilityWithInputs(@NonNull @SessionAttribute("user") User user,
												  @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		log.trace("getDetailedFacilityWithInputs(user: {}, facilityId: {})", user, facilityId);
		Facility facility = facilitiesService.getFacilityWithInputs(facilityId, user.getId());

		log.trace("getDetailedFacilityWithInputs() returns: {}", facility);
		return facility;
	}

	@GetMapping(path = "/api/allFacilities")
	public List<ProvidedService> allFacilities(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		log.trace("allFacilities({})", user.getId());

		List<ProvidedService> facilityList = facilitiesService.getAllFacilities(user.getId());

		log.trace("allFacilities() returns: {}", facilityList);
		return facilityList;
	}

	@PostMapping(path = "api/facility/regenerateClientSecret/{facilityId}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public PerunAttribute generateClientSecret(@NonNull @SessionAttribute("user") User user,
											   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		log.trace("generateClientSecret(user: {}, facilityId: {})", user, facilityId);
		PerunAttribute clientSecret = utilsService.regenerateClientSecret(user.getId(), facilityId);

		log.trace("generateClientSecret() returns: {}", clientSecret);
		return clientSecret;
	}

}
