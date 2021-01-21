package cz.metacentrum.perun.spRegistration.rest.controllers;


import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.AddAdminsService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.RemoveAdminsService;
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
	@NonNull private final RemoveAdminsService removeAdminsService;
	@NonNull private final UtilsService utilsService;

	@Autowired
	public FacilitiesController(@NonNull FacilitiesService facilitiesService,
								@NonNull AddAdminsService addAdminsService,
								@NonNull RemoveAdminsService removeAdminsService,
								@NonNull UtilsService utilsService)
	{
		this.addAdminsService = addAdminsService;
		this.removeAdminsService = removeAdminsService;
		this.facilitiesService = facilitiesService;
		this.utilsService = utilsService;
	}

	@GetMapping(path = "/api/facility/{facilityId}")
	public Facility facilityDetail(@NonNull @SessionAttribute("user") User user,
								   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return facilitiesService.getFacility(facilityId, user.getId(), true);
	}

	@GetMapping(path = "/api/facility/signature/{facilityId}")
	public Facility facilityDetailSignature(@NonNull @SessionAttribute("user") User user,
											@NonNull @PathVariable("facilityId") Long facilityId)
			throws InternalErrorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		return facilitiesService.getFacilityForSignature(facilityId, user.getId());
	}

	@GetMapping(path = "/api/userFacilities")
	public List<ProvidedService> userFacilities(@NonNull @SessionAttribute("user") User user)
			throws PerunUnknownException, PerunConnectionException
	{
		return facilitiesService.getAllUserFacilities(user.getId());
	}

	@PostMapping(path = "/api/addAdmins/{facilityId}")
	public boolean addAdmins(@NonNull @SessionAttribute("user") User user,
							 @NonNull @PathVariable("facilityId") Long facilityId,
							 @NonNull @RequestBody List<String> adminEmails)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user.getId())) {
			throw new UnauthorizedActionException();
		}
		return addAdminsService.addAdminsNotify(user, facilityId, adminEmails);
	}

	@PostMapping(path = "/api/addAdmin/confirm")
	public boolean addAdminConfirm(@NonNull @SessionAttribute("user") User user,
								   @NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, InternalErrorException, CodeNotStoredException, PerunUnknownException,
			PerunConnectionException
	{
		code = ApiUtils.normalizeRequestBodyString(code);
		return addAdminsService.confirmAddAdmin(user, code);
	}

	@PostMapping(path = "/api/addAdmin/reject")
	public void addAdminReject(@NonNull @SessionAttribute("user") User user,
							   @NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InvalidKeyException, InternalErrorException, CodeNotStoredException
	{
		code = ApiUtils.normalizeRequestBodyString(code);
		addAdminsService.rejectAddAdmin(user, code);
	}

	@GetMapping(path = "/api/addAdmin/getDetails/{code}")
	public LinkCode addAdminGetInfo(@NonNull @SessionAttribute("user") User user,
									@NonNull @PathVariable("code") String code)
	{
		return addAdminsService.getCodeByString(code);
	}

	@GetMapping(path = "/api/addAdmin/getFacilityDetails/{facilityId}")
	public Facility addAdminGetFacilityDetail(@NonNull @SessionAttribute("user") User user,
											  @NonNull @PathVariable("facilityId") Long facilityId)
			throws BadPaddingException, IllegalBlockSizeException, InternalErrorException,
			InvalidKeyException, PerunUnknownException, PerunConnectionException
	{
		return facilitiesService.getFacilityForSignature(facilityId, user.getId());
	}

	@GetMapping(path = "/api/facilityWithInputs/{facilityId}")
	public Facility getDetailedFacilityWithInputs(@NonNull @SessionAttribute("user") User user,
												  @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		return facilitiesService.getFacilityWithInputs(facilityId, user.getId());
	}

	@GetMapping(path = "/api/allFacilities")
	public List<ProvidedService> allFacilities(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return facilitiesService.getAllFacilities(user.getId());
	}

	@PostMapping(path = "api/facility/regenerateClientSecret/{facilityId}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public PerunAttribute generateClientSecret(@NonNull @SessionAttribute("user") User user,
											   @NonNull @PathVariable("facilityId") Long facilityId)
			throws UnauthorizedActionException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return utilsService.regenerateClientSecret(user.getId(), facilityId);
	}

	@PostMapping(path = "/api/removeAdmins/{facilityId}")
	public boolean removeAdmins(@NonNull @SessionAttribute("user") User user,
							 @NonNull @PathVariable("facilityId") Long facilityId,
							 @NonNull @RequestBody List<Long> adminsToRemoveIds)
			throws InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException {
		if (!utilsService.isAdminForFacility(facilityId, user.getId())) {
			throw new UnauthorizedActionException();
		}
		return removeAdminsService.removeAdmins(user, facilityId, adminsToRemoveIds);
	}

}
