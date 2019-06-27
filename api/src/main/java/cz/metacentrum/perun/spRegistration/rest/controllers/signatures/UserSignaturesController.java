package cz.metacentrum.perun.spRegistration.rest.controllers.signatures;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.CreateRequestException;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.MalformedCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.List;

/**
 * Controller handling USER actions related to Signatures.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
@RestController
public class UserSignaturesController {

	private static final Logger log = LoggerFactory.getLogger(UserSignaturesController.class);

	private final UserCommandsService service;

	@Autowired
	public UserSignaturesController(UserCommandsService service) {
		this.service = service;
	}

	@PostMapping(path = "/api/moveToProduction/createRequest/{facilityId}")
	public Long moveToProduction(@SessionAttribute("user") User user,
								 @PathVariable("facilityId") Long facilityId,
								 @RequestBody List<String> authorities)
			throws BadPaddingException, InvalidKeyException, ConnectorException, IllegalBlockSizeException,
			UnsupportedEncodingException, InternalErrorException, CreateRequestException, UnauthorizedActionException 
	{
		log.trace("moveToProduction(user: {}, facilityId: {} authorities: {})", user.getId(), facilityId, authorities);
		
		Long generatedId = service.requestMoveToProduction(facilityId, user.getId(), authorities);

		log.trace("moveToProduction() returns: {}", generatedId);
		return generatedId;
	}

	@GetMapping(path = "/api/moveToProduction/getFacilityDetails", params = "code")
	public Request signRequestGetData(String code)
			throws BadPaddingException, ConnectorException, IllegalBlockSizeException, MalformedCodeException,
			InvalidKeyException, ExpiredCodeException, UnsupportedEncodingException
	{
		log.trace("signRequestGetData({})", code);

		code = decodeCode(code);
		if (! service.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		Request request = service.getRequestDetailsForSignature(code);

		log.trace("signRequestGetData() returns: {}", request);
		return request;
	}

	@PostMapping(path = "/api/moveToProduction/approve")
	public boolean approveProductionTransfer(@SessionAttribute("user") User user,
											 @RequestBody String code)
			throws UnsupportedEncodingException, BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InternalErrorException, InvalidKeyException
	{
		log.trace("approveProductionTransfer(user: {}, code: {})", user, code);

		if (! service.validateCode(code)) {
			throw new IllegalAccessError("You cannot sign the request, code is invalid");
		}

		boolean successful = signTransferToProduction(code, user, true);

		log.trace("approveProductionTransfer() returns: {}", successful);
		return successful;
	}

	@PostMapping(path = "/api/moveToProduction/reject")
	public boolean rejectProductionTransfer(@SessionAttribute("user") User user,
											@RequestBody String code)
			throws UnsupportedEncodingException, BadPaddingException, ExpiredCodeException, IllegalBlockSizeException,
			MalformedCodeException, InternalErrorException, InvalidKeyException
	{
		log.trace("rejectProductionTransfer(user: {}, code: {})", user, code);

		if (! service.validateCode(code)) {
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

		List<RequestSignature> signaturesList = service.getApprovalsOfProductionTransfer(requestId, user.getId());

		log.trace("getApprovals() returns: {}", signaturesList);
		return signaturesList;
	}

	/* PRIVATE METHODS */

	private boolean signTransferToProduction(String code, User user, boolean approved) throws BadPaddingException,
			ExpiredCodeException, IllegalBlockSizeException, MalformedCodeException, InternalErrorException,
			InvalidKeyException, UnsupportedEncodingException
	{
		code = decodeCode(code);
		return service.signTransferToProduction(user, code, approved);
	}

	private String decodeCode(String code) throws UnsupportedEncodingException {
		if (code.startsWith("\"")) {
			code = code.substring(1, code.length() - 1);
		}

		return URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
	}
}
