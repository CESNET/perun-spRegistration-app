package cz.metacentrum.perun.spRegistration.rest.controllers.signatures;

import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.RequestSignaturesService;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.MalformedCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
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
 * Controller handling USER actions related to Signatures.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class UserSignaturesController {

	private static final Logger log = LoggerFactory.getLogger(UserSignaturesController.class);

	private final RequestsService requestsService;
	private final UtilsService utilsService;
	private final RequestSignaturesService requestSignaturesService;

	@Autowired
	public UserSignaturesController(RequestsService requestsService, UtilsService utilsService,
									RequestSignaturesService requestSignaturesService) {
		this.requestsService = requestsService;
		this.utilsService = utilsService;
		this.requestSignaturesService = requestSignaturesService;
	}

	@PostMapping(path = "/api/moveToProduction/createRequest/{facilityId}")
	public Long moveToProduction(@SessionAttribute("user") User user,
								 @PathVariable("facilityId") Long facilityId,
								 @RequestBody List<String> authorities)
			throws BadPaddingException, InvalidKeyException, ConnectorException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, ActiveRequestExistsException, UnauthorizedActionException
	{
		log.trace("moveToProduction(user: {}, facilityId: {} authorities: {})", user.getId(), facilityId, authorities);
		
		Long generatedId = requestsService.createMoveToProductionRequest(facilityId, user, authorities);

		log.trace("moveToProduction() returns: {}", generatedId);
		return generatedId;
	}

	@GetMapping(path = "/api/moveToProduction/getFacilityDetails", params = "code")
	public Request signRequestGetData(String code)
			throws BadPaddingException, ConnectorException, IllegalBlockSizeException, MalformedCodeException,
			InvalidKeyException, ExpiredCodeException, InternalErrorException {
		log.trace("signRequestGetData({})", code);

		code = ApiUtils.normalizeRequestBodyString(code);
		Request request = requestsService.getRequestForSignatureByCode(code);

		log.trace("signRequestGetData() returns: {}", request);
		return request;
	}

	@PostMapping(path = "/api/moveToProduction/approve")
	public boolean approveProductionTransfer(@SessionAttribute("user") User user,
											 @RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InternalErrorException, InvalidKeyException, CodeNotStoredException {
		log.trace("approveProductionTransfer(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		if (!utilsService.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		boolean successful = signTransferToProduction(code, user, true);

		log.trace("approveProductionTransfer() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/moveToProduction/reject")
	public boolean rejectProductionTransfer(@SessionAttribute("user") User user,
											@RequestBody String code)
			throws BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InternalErrorException, InvalidKeyException, CodeNotStoredException {
		log.trace("rejectProductionTransfer(user: {}, code: {})", user, code);

		code = ApiUtils.normalizeRequestBodyString(code);
		if (!utilsService.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		boolean successful = signTransferToProduction(code, user, false);

		log.trace("rejectProductionTransfer() returns: {}", successful);
		return successful;
	}

	@GetMapping(path = "/api/viewApprovals/{requestId}")
	public List<RequestSignature> getApprovals(@SessionAttribute("user") User user,
											   @PathVariable("requestId") Long requestId)
			throws UnauthorizedActionException, InternalErrorException
	{
		log.trace("getApprovals(user: {}, requestId: {})", user.getId(), requestId);

		List<RequestSignature> signaturesList = requestSignaturesService.getSignaturesForRequest(requestId, user.getId());

		log.trace("getApprovals() returns: {}", signaturesList);
		return signaturesList;
	}

	/* PRIVATE METHODS */

	private boolean signTransferToProduction(String code, User user, boolean approved) throws BadPaddingException,
			ExpiredCodeException, IllegalBlockSizeException, MalformedCodeException, InternalErrorException,
			InvalidKeyException {
		return requestSignaturesService.addSignature(user, code, approved);
	}
}
