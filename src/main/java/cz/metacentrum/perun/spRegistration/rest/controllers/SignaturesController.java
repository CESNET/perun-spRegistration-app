package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling actions related to Signatures.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class SignaturesController {

	@NonNull private final RequestsService requestsService;
	@NonNull private final UtilsService utilsService;
	@NonNull private final RequestSignaturesService requestSignaturesService;

	@Autowired
	public SignaturesController(@NonNull RequestsService requestsService,
								@NonNull UtilsService utilsService,
								@NonNull RequestSignaturesService requestSignaturesService)
	{
		this.requestsService = requestsService;
		this.utilsService = utilsService;
		this.requestSignaturesService = requestSignaturesService;
	}

	@PostMapping(path = "/api/moveToProduction/createRequest/{facilityId}")
	public Long moveToProduction(@NonNull @SessionAttribute("user") User user,
								 @NonNull @PathVariable("facilityId") Long facilityId,
								 @NonNull @RequestBody List<String> authorities)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, ActiveRequestExistsException,
			UnauthorizedActionException, PerunUnknownException, PerunConnectionException
	{
		if (!utilsService.isAdminForFacility(facilityId, user)) {
			throw new UnauthorizedActionException();
		}
		return requestsService.createMoveToProductionRequest(facilityId, user, authorities);
	}

	@GetMapping(path = "/api/moveToProduction/getFacilityDetails", params = "code")
	public RequestDTO signRequestGetData(@NonNull String code)
			throws BadPaddingException, IllegalBlockSizeException,
			InvalidKeyException, ExpiredCodeException, InternalErrorException
	{
		code = ApiUtils.normalizeRequestBodyString(code);
		return requestsService.getRequestForSignatureByCode(code);
	}

	@PostMapping(path = "/api/moveToProduction/approve")
	public boolean approveProductionTransfer(@NonNull @SessionAttribute("user") User user,
											 @NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InternalErrorException, InvalidKeyException
	{
		code = ApiUtils.normalizeRequestBodyString(code);
		if (!utilsService.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		return signTransferToProduction(code, user, true);
	}

	@PostMapping(path = "/api/moveToProduction/reject")
	public boolean rejectProductionTransfer(@NonNull @SessionAttribute("user") User user,
											@NonNull @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			InternalErrorException, InvalidKeyException
	{
		code = ApiUtils.normalizeRequestBodyString(code);
		if (!utilsService.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		return signTransferToProduction(code, user, false);
	}

	@GetMapping(path = "/api/viewApprovals/{requestId}")
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
		return requestSignaturesService.getSignaturesForRequest(requestId, user.getId());
	}

	/* PRIVATE METHODS */

	private boolean signTransferToProduction(@NonNull String code, @NonNull User user, boolean approved)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException, InternalErrorException,
			InvalidKeyException
	{
		return requestSignaturesService.addSignature(user, code, approved);
	}

}
