package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.configs.Config;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.CreateRequestException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.Mails;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UserCommandsService;
import cz.metacentrum.perun.spRegistration.service.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.MalformedCodeException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of UserCommandsService.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("Duplicates")
@Service("userService")
public class UserCommandsCommandsServiceImpl implements UserCommandsService {

	private static final Logger log = LoggerFactory.getLogger(UserCommandsCommandsServiceImpl.class);

	private static final String REQUEST_ID_KEY = "requestId";
	private static final String FACILITY_ID_KEY = "facilityId";
	private static final String CREATED_AT_KEY = "createdAt";
	private static final String REQUESTED_MAIL_KEY = "requestedMail";
	private static final String ACTION_KEY = "action";
	private static final String ACTION_ADD_ADMIN = "ADD";

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Config config;
	private final Properties messagesProperties;
	private final Cipher cipher;

	@Autowired
	public UserCommandsCommandsServiceImpl(RequestManager requestManager, PerunConnector perunConnector, Config config,
										   AppConfig appConfig, Properties messagesProperties) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.config = config;
		this.messagesProperties = messagesProperties;
		this.cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	}

	@Override
	public Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException, CreateRequestException {
		log.debug("createRegistrationRequest(userId: {}, attributes: {})", userId, attributes);
		if (userId == null || attributes == null) {
			log.error("Illegal input - userId: {}, attributes: {}", userId, attributes);
			throw new IllegalArgumentException("Illegal input - userId: " + userId + ", attributes: " + attributes);
		}

		addProxyIdentifierAttr(attributes);

		Request req = createRequest(null, userId, attributes, RequestAction.REGISTER_NEW_SP);
		validateCreatedRequestAndNotifyUser(req);

		log.debug("createRegistrationRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, RPCException, InternalErrorException, CreateRequestException {
		log.debug("createFacilityChangesRequest(facility: {}, userId: {}, attributes: {})", facilityId, userId, attributes);
		if (facilityId == null || userId == null || attributes == null) {
			log.error("Illegal input - facilityId: {}, userId: {}, attributes: {}", facilityId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId + ", attributes: " + attributes);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as facility admin, cannot create request");
			throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
		}

		Facility facility = perunConnector.getFacilityById(facilityId);
		if (facility == null) {
			log.error("Could not fetch facility for facilityId: {}", facilityId);
			throw new InternalErrorException("Could not fetch facility for facilityId: " + facilityId);
		}

		Request req = createRequest(facilityId, userId, attributes, RequestAction.UPDATE_FACILITY);
		validateCreatedRequestAndNotifyUser(req);

		log.debug("createFacilityChangesRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createRemovalRequest(Long userId, Long facilityId) throws UnauthorizedActionException, RPCException, InternalErrorException, CreateRequestException {
		log.debug("createRemovalRequest(userId: {}, facilityId: {})", userId, facilityId);
		if (facilityId == null || userId == null) {
			log.error("Illegal input - facilityId: {}, userId: {}", facilityId, userId);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as facility admin, cannot create request");
			throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
		}

		List<String> attrsToFetch = new ArrayList<>(appConfig.getPerunAttributeDefinitionsMap().keySet());
		Map<String, PerunAttribute> facilityAttributes = perunConnector.getFacilityAttributes(facilityId, attrsToFetch);

		Request req = createRequest(facilityId, userId, facilityAttributes, RequestAction.DELETE_FACILITY);
		validateCreatedRequestAndNotifyUser(req);

		log.debug("createRemovalRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, InternalErrorException {
		log.debug("updateRequest(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
		if (requestId == null || userId == null || attributes == null) {
			log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId +
					", attributes: " + attributes);
		}

		Request request = fetchRequestAndValidate(requestId);

		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		} else if (! isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User is not registered as admin in request, cannot update it");
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
		}

		log.debug("updating request");
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		for (Map.Entry<String, PerunAttribute> pair: convertedAttributes.entrySet()) {
			String key = pair.getKey();
			PerunAttribute attr = pair.getValue();
			if (request.getAttributes().containsKey(key)) {
				request.getAttributes().get(key).setValue(attr.getValue());
			} else {
				request.getAttributes().put(key, attr);
			}
		}

		request.setStatus(RequestStatus.WFA);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("updating request");
		boolean res = requestManager.updateRequest(request);
		if (!res) {
			log.error("FAILED WHEN updating request: {}", request);
		}

		log.debug("updateRequest returns: {}", res);
		return res;
	}

	@Override
	public Long requestMoveToProduction(Long facilityId, Long userId, List<String> authorities)
			throws UnauthorizedActionException, InternalErrorException, RPCException, CreateRequestException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException {
		log.debug("requestMoveToProduction(facilityId: {}, userId: {}, authorities: {})", facilityId, userId, authorities);
		if (facilityId == null || userId == null) {
			log.error("Illegal input - facilityId: {}, userId: {}", facilityId, userId);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as admin for facility, cannot ask for moving to production");
			throw new UnauthorizedActionException("User is not registered as admin for facility, cannot ask for moving to production");
		}

		Facility fac = getDetailedFacility(facilityId, userId);
		if (fac == null) {
			log.error("Could not retrieve facility for id: {}", facilityId);
			throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
		}

		Map<String, PerunAttribute> attributes = fac.getAttrs();
		String listAttrName = appConfig.getShowOnServicesListAttribute();
		String testSpAttrName = appConfig.getTestSpAttribute();

		PerunAttribute listAttr = attributes.get(listAttrName);
		listAttr.setOldValue(listAttr.getValue());
		listAttr.setValue(true);

		PerunAttribute testSpAttr = attributes.get(testSpAttrName);
		listAttr.setOldValue(testSpAttr.getValue());
		listAttr.setValue(false);

		Request req = createRequest(facilityId, userId, RequestAction.MOVE_TO_PRODUCTION, fac.getAttrs());
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		boolean res = Mails.transferToProductionUserNotify(req.getReqId(), req.getFacilityName(),
				req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);
		if (!res) {
			log.error("sending notification to user FAILED");
		}

		Map<String, String> authsWithLinks = generateLinksForAuthorities(authorities, req);

		for (Map.Entry<String, String> entry: authsWithLinks.entrySet()) {
			String mailAuthority = entry.getKey();
			String mailLink = entry.getValue();
			res = Mails.authoritiesApproveProductionTransferNotify(mailLink, req.getFacilityName(), mailAuthority, messagesProperties);
			if (!res) {
				log.error("sending notification to authority: {} FAILED", mailAuthority);
			}
		}

		log.debug("requestMoveToProduction returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Request getRequestDetailsForSignature(String code) throws InvalidKeyException,
			BadPaddingException, IllegalBlockSizeException, MalformedCodeException, ExpiredCodeException {
		log.debug("getRequestDetailsForSignature({})", code);
		JSONObject decrypted = decryptRequestCode(code);
		boolean isExpired = isExpiredCode(decrypted);

		if (isExpired) {
			throw new ExpiredCodeException("Code has expired");
		}

		Long requestId = decrypted.getLong(REQUEST_ID_KEY);
		Request result = requestManager.getRequestById(requestId);

		log.debug("getRequestDetailsForSignature returns: {}", result);
		return result;
	}

	@Override
	public boolean signTransferToProduction(User user, String code) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException, ExpiredCodeException {
		log.debug("signTransferToProduction(user: {}, code: {})", user, code);
		JSONObject decrypted = decryptRequestCode(code);
		boolean isExpired = isExpiredCode(decrypted);

		if (isExpired) {
			throw new ExpiredCodeException("Code has expired");
		}

		Long requestId = decrypted.getLong(REQUEST_ID_KEY);
		boolean result = requestManager.addSignature(requestId, user.getId(), user.getName());

		log.debug("signTransferToProduction returns: {}", result);
		return result;
	}

	@Override
	public Request getDetailedRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, InternalErrorException {
		log.debug("getDetailedRequest(requestId: {}, userId: {})", requestId, userId);

		Request request = fetchRequestAndValidate(requestId);

		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (!appConfig.isAdmin(userId) && !isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User cannot view request, user is not a requester");
			throw new UnauthorizedActionException("User cannot view request, user is not a requester");
		}

		log.debug("getDetailedRequest returns: {}", request);
		return request;
	}

	@Override
	public Facility getDetailedFacility(Long facilityId, Long userId)
			throws UnauthorizedActionException, RPCException, InternalErrorException {
		log.debug("getDetailedFacility(facilityId: {}, userId: {})", facilityId, userId);
		if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User cannot view facility, user is not an admin");
			throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
		}

		Facility facility = perunConnector.getFacilityById(facilityId);
		if (facility == null) {
			log.error("Could not retrieve facility for id: {}", facilityId);
			throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
		}

		Long activeRequestId = requestManager.getActiveRequestIdByFacilityId(facilityId);
		facility.setActiveRequestId(activeRequestId);

		List<String> attrsToFetch = new ArrayList<>(appConfig.getPerunAttributeDefinitionsMap().keySet());
		Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId, attrsToFetch);
		facility.setAttrs(attrs);
		boolean inTest = attrs.get(appConfig.getTestSpAttribute())
				.valueAsBoolean(false);
		facility.setTestEnv(inTest);

		log.debug("getDetailedFacility returns: {}", facility);
		return facility;
	}

	@Override
	public Facility getDetailedFacilityWithInputs(Long facilityId, Long userId) throws UnauthorizedActionException, RPCException, InternalErrorException {
		Facility facility = getDetailedFacility(facilityId, userId);
		for (Map.Entry<String, PerunAttribute> attr: facility.getAttrs().entrySet()) {
			AttrInput input = config.getInputMap().get(attr.getKey());
			attr.getValue().setInput(input);
		}

		return facility;
	}

	@Override
	public List<Request> getAllRequestsUserCanAccess(Long userId) throws RPCException {
		log.debug("getAllRequestsUserCanAccess({})", userId);
		if (userId == null) {
			log.error("userId is null");
			throw new IllegalArgumentException("userId is null");
		}
		List<Request> requests = requestManager.getAllRequestsByUserId(userId);
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);
		requests.addAll(requestManager.getAllRequestsByFacilityIds(whereAdmin));

		log.debug("getAllRequestsUserCanAccess returns: {}", requests);
		return new ArrayList<>(new HashSet<>(requests));
	}

	@Override
	public List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) throws RPCException {
		log.debug("getAllFacilitiesWhereUserIsAdmin({})", userId);
		if (userId == null) {
			log.error("userId is null");
			throw new IllegalArgumentException("userId is null");
		}
		List<Facility> userFacilities = perunConnector.getFacilitiesWhereUserIsAdmin(userId);
		Map<String, String> params = new HashMap<>();
		params.put(appConfig.getIdpAttribute(), appConfig.getIdpAttributeValue());
		List<Facility> proxyFacilities = perunConnector.getFacilitiesViaSearcher(params);
		List<Facility> result = userFacilities.stream()
				.filter(proxyFacilities::contains)
				.collect(Collectors.toList());

		log.debug("getAllFacilitiesWhereUserIsAdmin returns: {}", result);
		return result;
	}

	@Override
	public boolean addAdminsNotify(User user, Long facilityId, List<String> admins)
			throws UnauthorizedActionException, RPCException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, UnsupportedEncodingException, InternalErrorException {
		log.debug("addAdminsNotify(user: {}, facilityId: {}, admins: {}", user, facilityId, admins);
		boolean res = addRemoveAdminsNotify(user, facilityId, admins, ACTION_ADD_ADMIN);

		log.debug("addAdminsNotify returns: {}", res);
		return res;
	}

	private boolean addRemoveAdminsNotify(User user, Long facilityId, List<String> admins, String action)
			throws UnauthorizedActionException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, RPCException, UnsupportedEncodingException, InternalErrorException {
		log.debug("addRemoveAdminsNotify(user: {}, facilityId: {}, admins: {}", user, facilityId, admins);
		if (user == null || user.getId() == null || facilityId == null || admins == null || admins.isEmpty()) {
			log.error("Wrong parameters passed(user: {}, facilityId: {}, admins: {})", user, facilityId, admins);
			throw new IllegalArgumentException("Wrong parameters passed(user: " + user + ", facilityId: " + facilityId
					+ ", admins: " + admins +")");
		} else if (! isFacilityAdmin(facilityId, user.getId())) {
			log.error("User cannot request adding admins to facility, user is not an admin");
			throw new UnauthorizedActionException("User cannot request adding admins to facility, user is not an admin");
		}

		Facility facility = perunConnector.getFacilityById(facilityId);
		if (facility == null) {
			throw new InternalErrorException("Could not find facility for id: " + facilityId);
		}

		Map<String, String> mailsMap = new HashMap<>();
		for (String adminEmail: admins) {
			String code = createAddRemoveAdminCode(facilityId, adminEmail, action);
			code = URLEncoder.encode(code, StandardCharsets.UTF_8.toString());
			String link = appConfig.getAdminsEndpoint()
					.concat("?facilityName=").concat(facility.getName())
					.concat("&code=").concat(code);
			mailsMap.put(adminEmail, link);
			log.debug("Generated code: {}", code); //TODO: remove
		}

		boolean res = true;
		boolean isAddAdmins = ACTION_ADD_ADMIN.equalsIgnoreCase(action);
		for (Map.Entry<String, String> entry: mailsMap.entrySet()) {
			res = res && Mails.adminAddRemoveNotify(entry.getValue(), facility.getName(), entry.getKey(), isAddAdmins,
					messagesProperties);
		}

		log.debug("addRemoveAdminsNotify returns: {}", res);
		return res;
	}

	@Override
	public boolean confirmAddAdmin(User user, String code)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, RPCException {
		log.debug("confirmAddAdmin({})", code);
		JSONObject decrypted = decryptAddRemoveAdminCode(code);
		boolean isExpired = isExpiredCode(decrypted);

		if (isExpired) {
			throw new ExpiredCodeException("Code has expired");
		}

		Long facilityId = decrypted.getLong(FACILITY_ID_KEY);
		String action = decrypted.getString(ACTION_KEY);

		boolean result;
		if (ACTION_ADD_ADMIN.equalsIgnoreCase(action)) {
			result = perunConnector.addFacilityAdmin(facilityId, user.getId());
		} else {
			throw new MalformedCodeException("No valid action has been found in code");
		}

		log.debug("confirmAddAdmin returns: {}", result);
		return result;
	}

	/* PRIVATE METHODS */

	private Request createRequest(Long facilityId, Long userId, List<PerunAttribute> attributes, RequestAction action)
			throws InternalErrorException, CreateRequestException {
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		return createRequest(facilityId, userId, action, convertedAttributes);
	}

	private Request createRequest(Long facilityId, Long userId, Map<String, PerunAttribute> attributes, RequestAction action)
			throws InternalErrorException, CreateRequestException {
		return createRequest(facilityId, userId, action, attributes);
	}

	private Request createRequest(Long facilityId, Long userId, RequestAction action, Map<String,PerunAttribute> attributes)
			throws InternalErrorException, CreateRequestException {
		log.debug("createRequest(facilityId: {}, userId: {}, action: {}, attributes: {})", facilityId, userId, action, attributes);
		Request request = new Request();
		request.setFacilityId(facilityId);
		request.setStatus(RequestStatus.WFA);
		request.setAction(action);
		request.setAttributes(attributes);
		request.setReqUserId(userId);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("creating request in DB");
		Long requestId = requestManager.createRequest(request);
		if (requestId == null) {
			log.error("Could not create request: {} in DB", request);
			throw new InternalErrorException("Could not create request in DB");
		}

		request.setReqId(requestId);

		log.debug("createRequest returns: {}", request);
		return request;
	}

	private boolean isFacilityAdmin(Long facilityId, Long userId) throws RPCException {
		log.debug("isFacilityAdmin(facilityId: {}, userId: {})", facilityId, userId);
		log.debug("fetching facilities where user with id: {} is admin from Perun", userId);
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);

		if (whereAdmin == null || whereAdmin.isEmpty()) {
			log.debug("isFacilityAdmin returns: {}", false);
			return false;
		}

		boolean result = whereAdmin.contains(facilityId);
		log.debug("isFacilityAdmin returns:  {}", result);
		return result;
	}

	private boolean isAdminInRequest(Long reqUserId, Long userId) {
		log.debug("isAdminInRequest(reqUserId: {}, userId: {})", reqUserId, userId);

		boolean res = reqUserId.equals(userId);
		log.debug("isAdminInRequest returns: {}", res);
		return res;
	}

	private void addProxyIdentifierAttr(List<PerunAttribute> attributes) {
		String identifierAttrName = appConfig.getIdpAttribute();
		String value = appConfig.getIdpAttributeValue();

		PerunAttribute identifierAttr = new PerunAttribute();
		identifierAttr.setFullName(identifierAttrName);
		identifierAttr.setValue(Collections.singletonList(value));

		attributes.add(identifierAttr);
	}

	private Request fetchRequestAndValidate(Long requestId) throws InternalErrorException {
		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		}
		return request;
	}

	private void validateCreatedRequestAndNotifyUser(Request req) throws InternalErrorException {
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		log.debug("sending mail notification");
		boolean res = Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(), req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);
		if (!res) {
			log.error("sending notification to user FAILED");
		}
	}

	private Map<String, String> generateLinksForAuthorities(List<String> authorities, Request request)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, RPCException {
		SecretKeySpec secret = appConfig.getSecret();
		cipher.init(Cipher.ENCRYPT_MODE, secret);

		if (authorities == null || authorities.isEmpty()) {
			String prop = messagesProperties.getProperty("moveToProduction.authorities");
			authorities = Arrays.asList(prop.split(","));
		}

		Facility facility = perunConnector.getFacilityById(request.getFacilityId());

		Map<String, String> linksMap = new HashMap<>();
		for (String authority: authorities) {
			String code = createRequestCode(request.getReqId(), request.getFacilityId(), authority);
			code = URLEncoder.encode(code, StandardCharsets.UTF_8.toString());
			String link = appConfig.getSignaturesEndpointUrl()
					.concat("?facilityName=").concat(facility.getName())
					.concat("&code=").concat(code);
			linksMap.put(authority, link);
			log.debug("Generated code: {}", code); //TODO: remove
		}

		return linksMap;
	}

	private JSONObject decryptRequestCode(String code)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, MalformedCodeException {
		cipher.init(Cipher.DECRYPT_MODE, appConfig.getSecret());
		Base64.Decoder b64dec = Base64.getUrlDecoder();
		byte[] decrypted = cipher.doFinal(b64dec.decode(code));
		String objInString = new String(decrypted);

		try {
			return new JSONObject(objInString);
		} catch (JSONException e) {
			throw new MalformedCodeException();
		}
	}

	private String createRequestCode(Long requestId, Long facilityId, String requestedMail)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		cipher.init(Cipher.ENCRYPT_MODE, appConfig.getSecret());
		JSONObject object = new JSONObject();
		object.put(REQUEST_ID_KEY, requestId);
		object.put(FACILITY_ID_KEY, facilityId);
		object.put(CREATED_AT_KEY, LocalDateTime.now().toString());
		object.put(REQUESTED_MAIL_KEY, requestedMail);

		String strToEncrypt = object.toString();
		Base64.Encoder b64enc = Base64.getUrlEncoder();
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
		return b64enc.encodeToString(encrypted);
	}

	private JSONObject decryptAddRemoveAdminCode(String code)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, MalformedCodeException {
		cipher.init(Cipher.DECRYPT_MODE, appConfig.getSecret());
		Base64.Decoder b64dec = Base64.getUrlDecoder();
		byte[] decrypted = cipher.doFinal(b64dec.decode(code));
		String objInString = new String(decrypted);

		try {
			return new JSONObject(objInString);
		} catch (JSONException e) {
			throw new MalformedCodeException();
		}
	}

	private String createAddRemoveAdminCode(Long facilityId, String requestedMail, String action)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		cipher.init(Cipher.ENCRYPT_MODE, appConfig.getSecret());
		JSONObject object = new JSONObject();
		object.put(FACILITY_ID_KEY, facilityId);
		object.put(CREATED_AT_KEY, LocalDateTime.now().toString());
		object.put(REQUESTED_MAIL_KEY, requestedMail);
		object.put(ACTION_KEY, action);

		String strToEncrypt = object.toString();
		Base64.Encoder b64enc = Base64.getUrlEncoder();
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
		return b64enc.encodeToString(encrypted);
	}

	private boolean isExpiredCode(JSONObject decrypted) {
		LocalDateTime createdAt = LocalDateTime.parse(decrypted.getString(CREATED_AT_KEY));
		long daysValid = appConfig.getConfirmationPeriodDays();
		long hoursValid = appConfig.getConfirmationPeriodHours();
		LocalDateTime validUntil = createdAt.plusDays(daysValid).plusHours(hoursValid);

		return LocalDateTime.now().isAfter(validUntil);
	}
}
