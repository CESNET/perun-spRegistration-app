package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignatureDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.rest.ApiEntityMapper;
import cz.metacentrum.perun.spRegistration.rest.models.RequestOverview;
import cz.metacentrum.perun.spRegistration.rest.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.service.RequestSignaturesService;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling actions related to Requests.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
@RequestMapping("/api/request")
public class RequestsController {

	@NonNull private final RequestsService requestsService;
	@NonNull private final UtilsService utilsService;
	@NonNull private final AttributesProperties attributesProperties;
	@NonNull private final RequestSignaturesService requestSignaturesService;

	@Autowired
	public RequestsController(@NonNull RequestsService requestsService,
							  @NonNull UtilsService utilsService,
							  @NonNull AttributesProperties attributesProperties,
							  @NonNull RequestSignaturesService requestSignaturesService)
	{
		this.requestsService = requestsService;
		this.utilsService = utilsService;
		this.attributesProperties = attributesProperties;
		this.requestSignaturesService = requestSignaturesService;
	}

	@GetMapping(path = "/user")
	public List<RequestOverview> userRequests(@NonNull @SessionAttribute("user") User user)
			throws PerunUnknownException, PerunConnectionException
	{
		List<RequestDTO> requests = requestsService.getAllUserRequests(user);
		return ApiEntityMapper.mapRequestDtosToRequestOverviews(requests, attributesProperties);
	}

	@PostMapping(path = "/register")
	public Long createRegistrationRequest(@NonNull @SessionAttribute("user") User user,
										  @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException
	{
		return requestsService.createRegistrationRequest(user, attributes);
	}

	@PostMapping(path = "/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@NonNull @SessionAttribute("user") User user,
											 @NonNull @RequestBody List<PerunAttribute> attributes,
											 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.createFacilityChangesRequest(facilityId, user, attributes);
	}

	@PostMapping(path = "/remove/{facilityId}")
	public Long createRemovalRequest(@NonNull @SessionAttribute("user") User user,
									 @NonNull @PathVariable("facilityId") Long facilityId)
			throws ActiveRequestExistsException, InternalErrorException, UnauthorizedActionException,
			PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.createRemovalRequest(user, facilityId);
	}

	@PostMapping(path = "/update/{requestId}")
	public boolean updateRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		// auth done at the service level, cannot do it here
		return requestsService.updateRequest(requestId, user, attributes);
	}

	@GetMapping(path = "/request/{requestId}")
	public RequestDTO requestDetail(@NonNull @SessionAttribute("user") User user,
									@NonNull @PathVariable("requestId") Long requestId)
			throws InternalErrorException, UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		// auth done at the service level, cannot do it here
		return requestsService.getRequest(requestId, user);
	}

	@PostMapping(path = "/cancel/{requestId}")
	public boolean cancelRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
			PerunUnknownException, PerunConnectionException
	{
		// auth done at the service level, cannot do it here
		return requestsService.cancelRequest(requestId, user);
	}

	// admin

	@GetMapping
	public List<RequestOverview> allRequests(@NonNull @SessionAttribute("user") User user)
			throws UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		List<RequestDTO> requests = requestsService.getAllRequests(user);
		return ApiEntityMapper.mapRequestDtosToRequestOverviews(requests, attributesProperties);
	}

	@PostMapping(path = "/approve/{requestId}")
	public boolean approveRequest(@NonNull @SessionAttribute("user") User user,
								  @NonNull @PathVariable("requestId") Long requestId)
			throws CannotChangeStatusException, InternalErrorException, UnauthorizedActionException,
			BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
			PerunConnectionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.approveRequest(requestId, user);
	}

	@PostMapping(path = "/reject/{requestId}")
	public boolean rejectRequest(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.rejectRequest(requestId, user);
	}

	@PostMapping(path = "/askForChanges/{requestId}")
	public boolean askForChanges(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("requestId") Long requestId,
								 @NonNull @RequestBody List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.askForChanges(requestId, user, attributes);
	}

	@GetMapping(path = "/{requestId}/signatures")
	public List<RequestSignature> getApprovals(@NonNull @SessionAttribute("user") User user,
											   @NonNull @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException
	{
		RequestDTO req = requestsService.getRequest(requestId, user);
		if (req == null) {
			throw new InternalErrorException("Request has not been found");
		} else if (!utilsService.isAdminForRequest(req, user)) {
			throw new UnauthorizedActionException("User is not authorized to perform this action");
		}
		List<RequestSignatureDTO> signatures = requestSignaturesService.getSignaturesForRequest(requestId, user.getId());
		return ApiEntityMapper.mapRequestSignatureDtoToRequestSignature(signatures);
	}

}
