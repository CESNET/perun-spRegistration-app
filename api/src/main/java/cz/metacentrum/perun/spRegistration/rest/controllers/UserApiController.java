package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.SpRegistrationApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;

@RestController
@SessionAttributes("user")
public class UserApiController {

	private static final Logger log = LoggerFactory.getLogger(UserApiController.class);

	private final UserCommandsService service;
	@Value("${dev.enabled}")
	private boolean devEnabled;

	@Autowired
	public UserApiController(UserCommandsService service) {
		this.service = service;
	}

	@RequestMapping(path = "/api/userFacilities", method = RequestMethod.GET)
	public List<Facility> userFacilities(@SessionAttribute("user") User user) throws SpRegistrationApiException {
		log.debug("userFacilities({})", user.getId());
		try {
			return service.getAllFacilitiesWhereUserIsAdmin(user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/userRequests", method = RequestMethod.GET)
	public List<Request> userRequests(@SessionAttribute("user") User user) throws SpRegistrationApiException {
		log.debug("userRequests({})", user.getId());
		try {
			return service.getAllRequestsUserCanAccess(user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/register")
	public Long createRegistrationRequest(@SessionAttribute("user") User user,
										  @RequestBody List<PerunAttribute> attributes) throws SpRegistrationApiException {
		log.debug("createRegistrationRequest(user: {}, attributes: {})", user.getId(), attributes);
		try {
			return service.createRegistrationRequest(user.getId(), attributes);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/changeFacility/{facilityId}")
	public Long createFacilityChangesRequest(@SessionAttribute("user") User user, @RequestBody List<PerunAttribute> attributes,
											 @PathVariable("facilityId") Long facilityId) throws SpRegistrationApiException {
		log.debug("createFacilityChangesRequest(user: {}, facilityId: {}, attributes: {})", user.getId(), facilityId, attributes);
		try {
			return service.createFacilityChangesRequest(facilityId, user.getId(), attributes);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/remove/{facilityId}")
	public Long createRemovalRequest(@SessionAttribute("user") User user,
									 @PathVariable("facilityId") Long facilityId) throws SpRegistrationApiException {
		log.debug("createRemovalRequest(user: {}, facilityId: {})", user.getId(), facilityId);
		try {
			return service.createRemovalRequest(user.getId(), facilityId);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/update/{requestId}")
	public boolean updateRequest(@SessionAttribute("user") User user, @RequestBody List<PerunAttribute> attributes,
								@PathVariable("requestId") Long requestId) throws SpRegistrationApiException {
		log.debug("updateRequest(user: {}, requestId: {}, attributes: {})", user.getId(), requestId, attributes);
		try {
			return service.updateRequest(requestId, user.getId(), attributes);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/askApproval/{requestId}")
	public boolean askForApproval(@SessionAttribute("user") User user,
								  @PathVariable("requestId") Long requestId) throws SpRegistrationApiException {
		log.debug("askForApproval(user: {}, requestId: {})", user.getId(), requestId);
		try {
			return service.askForApproval(requestId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/cancel/{requestId}")
	public boolean cancelRequest(@SessionAttribute("user") User user,
								@PathVariable("requestId") Long requestId) throws SpRegistrationApiException {
		log.debug("cancelRequest(user: {}, requestId: {})", user.getId(), requestId);
		try {
			return service.cancelRequest(requestId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/renew/{requestId}")
	public boolean renewRequest(@SessionAttribute("user") User user,
							    @PathVariable("requestId") Long requestId) throws SpRegistrationApiException {
		log.debug("renewRequest(user: {}, requestId: {})", user.getId(), requestId);
		try {
			return service.renewRequest(requestId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/facility/{facilityId}")
	public Facility facilityDetail(@SessionAttribute("user") User user,
								   @PathVariable("facilityId") Long facilityId) throws SpRegistrationApiException {
		log.debug("facilityDetail(user(): {}, facilityId: {})", user.getId(), facilityId);
		try {
			return service.getDetailedFacility(facilityId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/request/{requestId}")
	public Request requestDetail(@SessionAttribute("user") User user,
								 @PathVariable("requestId") Long requestId) throws SpRegistrationApiException {
		log.debug("requestDetail(user: {}, requestId: {})", user.getId(), requestId);
		try {
			return service.getDetailedRequest(requestId, user.getId());
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/moveToProduction/createRequest/{facilityId}", method = RequestMethod.POST)
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

	@RequestMapping(path = "/api/moveToProduction/getFacilityDetails/{facilityId}", method = RequestMethod.GET)
	public Facility signRequestGetData(@PathVariable("facilityId") Long facilityId) throws SpRegistrationApiException {
		log.debug("signRequestGetData(facilityId: {})", facilityId);
		try {
			return service.getFacilityDetailsForSignature(facilityId);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

	@RequestMapping(path = "/api/moveToProduction/approve/{facilityId}", method = RequestMethod.POST)
	public boolean signApprovalForProduction(@SessionAttribute("user") User user,
											 @PathVariable("facilityId") Long facilityId,
											 @RequestBody String hash) throws SpRegistrationApiException {
		log.debug("signApprovalForProduction(user: {}, facilityId: {}, hash: {})", user, facilityId, hash);
		try {
			return service.signTransferToProduction(facilityId, hash, user);
		} catch (Exception e) {
			throw new SpRegistrationApiException(e);
		}
	}

}
