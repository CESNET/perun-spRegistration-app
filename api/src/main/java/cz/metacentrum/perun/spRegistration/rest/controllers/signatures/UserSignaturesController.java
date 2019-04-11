package cz.metacentrum.perun.spRegistration.rest.controllers.signatures;

import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.SpRegistrationApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
								 @RequestBody List<String> authorities) throws SpRegistrationApiException {
		log.debug("moveToProduction(user: {}, facilityId: {} authorities: {})", user.getId(), facilityId, authorities);
		try {
			return service.requestMoveToProduction(facilityId, user.getId(), authorities);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@GetMapping(path = "/api/moveToProduction/getFacilityDetails", params = "code")
	public Request signRequestGetData(String code) throws SpRegistrationApiException {
		log.debug("signRequestGetData({})", code);
		try {
			code = URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
			return service.getRequestDetailsForSignature(code);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@PostMapping(path = "/api/moveToProduction/approve")
	public boolean signApprovalForProduction(@SessionAttribute("user") User user,
											 @RequestBody String code) throws SpRegistrationApiException {
		log.debug("signApprovalForProduction(user: {}, code: {})", user, code);
		try {
			if (code.startsWith("\"")) {
				code = code.substring(1, code.length() - 1);
			}
			code = URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
			return service.signTransferToProduction(user, code);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}
}
