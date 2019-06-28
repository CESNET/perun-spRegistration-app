package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.configs.Config;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
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
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@SuppressWarnings("Duplicates")
@Service("userService")
public class UserCommandsServiceImpl implements UserCommandsService {

	private static final Logger log = LoggerFactory.getLogger(UserCommandsServiceImpl.class);

	private static final String REQUEST_ID_KEY = "requestId";
	private static final String FACILITY_ID_KEY = "facilityId";
	private static final String CREATED_AT_KEY = "createdAt";
	private static final String REQUESTED_MAIL_KEY = "requestedMail";

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Config config;
	private final Properties messagesProperties;
	private final Cipher cipher;


	@Autowired
	public UserCommandsServiceImpl(RequestManager requestManager, PerunConnector perunConnector, Config config,
								   AppConfig appConfig, Properties messagesProperties)
			throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.config = config;
		this.messagesProperties = messagesProperties;
		this.cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	}

	@Override
	public Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException {
		log.trace("createRegistrationRequest(userId: {}, attributes: {})", userId, attributes);

		if (Utils.checkParamsInvalid(userId, attributes)) {
			log.error("Wrong parameters passed: (userId: {}, attributes: {})", userId, attributes);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		attributes.add(generateProxyIdentifierAttr());

		Request req = null;
		try {
			req = createRequest(null, userId, attributes, RequestAction.REGISTER_NEW_SP);
		} catch (ActiveRequestExistsException e) {
			//this cannot happen as the registration is for new service and thus facility id will be always null
		}

		if (req == null || req.getReqId() == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		boolean notificationSent = Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(),
				req.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		if (!notificationSent) {
			log.error("Some operations failed - notificationsSent: false");
		} else {
			log.info("Request created, notifications sent");
		}

		log.trace("createRegistrationRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException
	{
		log.trace("createFacilityChangesRequest(facility: {}, userId: {}, attributes: {})", facilityId, userId, attributes);

		if (Utils.checkParamsInvalid(facilityId, userId, attributes)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {}, attributes: {})",
					facilityId, userId, attributes);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
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
		if (req == null || req.getReqId() == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		boolean notificationSent = Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(),
				req.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		if (! notificationSent) {
			log.error("Some operations failed - notificationsSent: false");
		} else {
			log.info("Request created, notifications sent");
		}

		log.trace("createFacilityChangesRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createRemovalRequest(Long userId, Long facilityId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException
	{
		log.trace("createRemovalRequest(userId: {}, facilityId: {})", userId, facilityId);

		if (Utils.checkParamsInvalid(userId, facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as facility admin, cannot create request");
			throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
		}

		List<String> attrsToFetch = new ArrayList<>(appConfig.getPerunAttributeDefinitionsMap().keySet());
		Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId, attrsToFetch);
		boolean isOidc = ServiceUtils.isOidcAttributes(attrs, appConfig.getEntityIdAttributeName());
		List<String> keptAttrs = getAttrsToKeep(isOidc);
		Map<String, PerunAttribute> facilityAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);

		Request req = createRequest(facilityId, userId, RequestAction.DELETE_FACILITY, facilityAttributes);
		if (req == null || req.getReqId() == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		log.info("Sending mail notification");
		boolean notificationSent = Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(),
				req.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		if (! notificationSent) {
			log.error("Some operations failed - notificationsSent: false");
		} else {
			log.info("Request created, notifications sent");
		}

		log.trace("createRemovalRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, InternalErrorException {
		log.trace("updateRequest(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
		if (requestId == null || userId == null || attributes == null) {
			log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId +
					", attributes: " + attributes);
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		} else if (! isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User is not registered as admin in request, cannot update it");
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
		}

		log.info("Updating request");
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMapAttrs(attributes, appConfig);
		request.updateAttributes(convertedAttributes, true);

		request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		boolean requestUpdated = requestManager.updateRequest(request);

		log.info("Sending mail notification");
		boolean notificationSent = Mails.requestStatusUpdateUserNotify(requestId, request.getStatus(),
				request.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		boolean successful = requestUpdated && notificationSent;
		if (!successful) {
			log.error("Some operations failed - requestUpdated: {}, notificationSent: {}", requestUpdated, notificationSent);
		} else {
			log.info("Request updated, notification sent");
		}

		log.trace("updateRequest returns: {}", requestUpdated);
		return requestUpdated;
	}

	@Override
	public Long requestMoveToProduction(Long facilityId, Long userId, List<String> authorities)
			throws UnauthorizedActionException, InternalErrorException, ConnectorException, ActiveRequestExistsException,
			BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException
	{
		log.trace("requestMoveToProduction(facilityId: {}, userId: {}, authorities: {})", facilityId, userId, authorities);

		if (Utils.checkParamsInvalid(facilityId, userId)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as admin for facility, cannot ask for moving to production");
			throw new UnauthorizedActionException("User is not registered as admin for facility, cannot ask for moving to production");
		}

		Facility fac = getDetailedFacility(facilityId, userId);
		if (fac == null) {
			log.error("Could not retrieve facility for id: {}", facilityId);
			throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
		}

		Map<String, PerunAttribute> filteredAttributes = filterNotNullAttributes(fac);

		Request req = createRequest(facilityId, userId, RequestAction.MOVE_TO_PRODUCTION, filteredAttributes);
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		if (authorities == null || authorities.isEmpty()) {
			String prop = messagesProperties.getProperty("moveToProduction.authorities");
			authorities = Arrays.asList(prop.split(","));
		}

		Map<String, String> authoritiesCodesMap = generateCodesForAuthorities(req, authorities);
		Map<String, String> authoritiesLinksMap = generateLinksForAuthorities(authoritiesCodesMap, req);

		boolean userNotificationSent = Mails.transferToProductionUserNotify(req.getReqId(), req.getFacilityName(),
				req.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);
		int authoritiesNotificationSentCount = sendAuthoritiesNotifications(authoritiesLinksMap, req);
		boolean successful = (userNotificationSent && (authoritiesNotificationSentCount == authorities.size()));

		if (!successful) {
			log.error("Some operations failed - userNotificationSent: {}, authoritiesNotificationSentCount: {} out of {}",
					userNotificationSent, authoritiesNotificationSentCount, authorities.size());
		} else {
			log.info("Request updated, notification sent to user, notifications sent to authorities");
		}

		log.trace("requestMoveToProduction returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Request getRequestDetailsForSignature(String code)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, MalformedCodeException,
			ExpiredCodeException, InternalErrorException
	{
		log.trace("getRequestDetailsForSignature({})", code);

		if (Utils.checkParamsInvalid(code)) {
			log.error("Wrong parameters passed: (code: {})", code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject decryptedCode = decryptRequestCode(code);
		boolean isExpired = isExpiredCode(decryptedCode);

		if (isExpired) {
			log.error("User trying to approve request with expired code: {}", decryptedCode);
			throw new ExpiredCodeException("Code has expired");
		}

		Long requestId = decryptedCode.getLong(REQUEST_ID_KEY);
		log.debug("Fetching request for id: {}", requestId);
		Request request = requestManager.getRequestById(requestId);

		if (request == null) {
			log.error("Cannot find request from code");
			throw new InternalErrorException("Cannot find request from code");
		}

		log.trace("getRequestDetailsForSignature returns: {}", request);
		return request;
	}

	@Override
	public boolean signTransferToProduction(User user, String code, boolean approved)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, InternalErrorException
	{
		log.trace("signTransferToProduction(user: {}, code: {})", user, code);

		if (Utils.checkParamsInvalid(user, code)) {
			log.error("Wrong parameters passed: (user: {}, code: {})", user, code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject decryptedCode = decryptRequestCode(code);
		boolean isExpired = isExpiredCode(decryptedCode);

		if (isExpired) {
			log.error("User trying to approve request with expired code: {}", decryptedCode);
			throw new ExpiredCodeException("Code has expired");
		}

		Long requestId = decryptedCode.getLong(REQUEST_ID_KEY);
		boolean signed = requestManager.addSignature(requestId, user.getId(), user.getName(), approved, code);

		log.trace("signTransferToProduction returns: {}", signed);
		return signed;
	}

	@Override
	public List<RequestSignature> getApprovalsOfProductionTransfer(Long requestId, Long userId)
			throws UnauthorizedActionException, InternalErrorException
	{
		log.trace("getApprovalsOfProductionTransfer(requestId: {}, userId: {})", requestId, userId);

		if (Utils.checkParamsInvalid(requestId, userId)) {
			log.error("Wrong parameters passed: (requestId: {}, userId: {})" , requestId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
		} else if (!appConfig.isAppAdmin(userId) && !isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User is not authorized to view approvals for request: {}", requestId);
			throw new UnauthorizedActionException(Utils.GENERIC_ERROR_MSG);
		}

		List<RequestSignature> result = requestManager.getRequestSignatures(requestId);

		log.trace("getApprovalsOfProductionTransfer returns: {}", result);
		return result;
	}

	@Override
	public Request getDetailedRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, InternalErrorException
	{
		log.trace("getDetailedRequest(requestId: {}, userId: {})", requestId, userId);

		if (Utils.checkParamsInvalid(requestId, userId)) {
			log.error("Wrong parameters passed: (requestId: {}, userId: {})", requestId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		} else if (!appConfig.isAppAdmin(userId) && !isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User cannot view request, user is not requester nor appAdmin");
			throw new UnauthorizedActionException("User cannot view request, user is not a requester");
		}

		log.trace("getDetailedRequest returns: {}", request);
		return request;
	}

	@Override
	public Facility getDetailedFacility(Long facilityId, Long userId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException
	{
		log.trace("getDetailedFacility(facilityId: {}, userId: {})", facilityId, userId);

		if (Utils.checkParamsInvalid(facilityId, userId)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		} else if (! appConfig.isAppAdmin(userId) && ! isFacilityAdmin(facilityId, userId)) {
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
		boolean isOidc = ServiceUtils.isOidcAttributes(attrs, appConfig.getEntityIdAttributeName());
		List<String> keptAttrs = getAttrsToKeep(isOidc);

		facility.setAttrs(ServiceUtils.filterFacilityAttrs(attrs, keptAttrs));

		boolean inTest = attrs.get(appConfig.getIsTestSpAttributeName()).valueAsBoolean();
		facility.setTestEnv(inTest);

		log.trace("getDetailedFacility returns: {}", facility);
		return facility;
	}

	@Override
	public Facility getDetailedFacilityWithInputs(Long facilityId, Long userId)
			throws UnauthorizedActionException, ConnectorException, InternalErrorException
	{
		log.trace("getDetailedFacilityWithInputs(facilityId: {}, userId: {})", facilityId, userId);

		if (Utils.checkParamsInvalid(facilityId, userId)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Facility facility = getDetailedFacility(facilityId, userId);
		if (facility == null || facility.getAttrs() == null) {
			log.error("Could not fetch facility for id: {}", facilityId);
			throw new InternalErrorException("Could not fetch facility for id: " + facilityId);
		}

		for (Map.Entry<String, PerunAttribute> attr: facility.getAttrs().entrySet()) {
			AttrInput input = config.getInputMap().get(attr.getKey());
			attr.getValue().setInput(input);
		}

		log.trace("getDetailedFacilityWithInputs() returns: {}", facility);
		return facility;
	}

	@Override
	public List<Request> getAllRequestsUserCanAccess(Long userId) throws ConnectorException {
		log.trace("getAllRequestsUserCanAccess({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Set<Request> requests = new HashSet<>();

		List<Request> userRequests = requestManager.getAllRequestsByUserId(userId);
		if (userRequests != null && !userRequests.isEmpty()) {
			requests.addAll(userRequests);
		}

		Set<Long> facilityIdsWhereUserIsAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);
		if (facilityIdsWhereUserIsAdmin != null && !facilityIdsWhereUserIsAdmin.isEmpty()) {
			List<Request> facilitiesRequests = requestManager.getAllRequestsByFacilityIds(facilityIdsWhereUserIsAdmin);
			if (facilitiesRequests != null && !facilitiesRequests.isEmpty()) {
				requests.addAll(facilitiesRequests);
			}
		}

		List<Request> unique = new ArrayList<>(requests);
		log.trace("getAllRequestsUserCanAccess returns: {}", unique);
		return unique;
	}

	@Override
	public List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException {
		log.trace("getAllFacilitiesWhereUserIsAdmin({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		List<Facility> proxyFacilities = perunConnector.getFacilitiesByProxyIdentifier(
				appConfig.getProxyIdentifierAttributeName(), appConfig.getProxyIdentifierAttributeValue());
		Map<Long, Facility> proxyFacilitiesMap = ServiceUtils.transformListToMapFacilities(proxyFacilities);
		if (proxyFacilitiesMap == null || proxyFacilitiesMap.isEmpty()) {
			return new ArrayList<>();
		}

		List<Facility> userFacilities = perunConnector.getFacilitiesWhereUserIsAdmin(userId);
		if (userFacilities == null || userFacilities.isEmpty()) {
			return new ArrayList<>();
		}

		List<Facility> testFacilities = perunConnector.getFacilitiesByAttribute(
				appConfig.getIsTestSpAttributeName(), "true");
		Map<Long, Facility> testFacilitiesMap = ServiceUtils.transformListToMapFacilities(testFacilities);
		if (testFacilitiesMap == null) {
			testFacilitiesMap = new HashMap<>();
		}

		List<Facility> filteredFacilities = new ArrayList<>();

		for (Facility f : userFacilities) {
			if (proxyFacilitiesMap.containsKey(f.getId())) {
				f.setTestEnv(testFacilitiesMap.containsKey(f.getId()));
				filteredFacilities.add(f);
			}
		}

		log.trace("getAllFacilitiesWhereUserIsAdmin returns: {}", filteredFacilities);
		return filteredFacilities;
	}

	@Override
	public boolean addAdminsNotify(User user, Long facilityId, List<String> admins)
			throws UnauthorizedActionException, ConnectorException, BadPaddingException, InvalidKeyException,
			IllegalBlockSizeException, UnsupportedEncodingException, InternalErrorException
	{
		log.trace("addAdminsNotify(user: {}, facilityId: {}, admins: {}", user, facilityId, admins);

		if (Utils.checkParamsInvalid(user, facilityId, admins) || admins.isEmpty()) {
			log.error("Wrong parameters passed (user: {}, facilityId: {}, admins: {})", user, facilityId, admins);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		} else if (! isFacilityAdmin(facilityId, user.getId())) {
			log.error("User cannot request adding admins to facility, user is not an admin");
			throw new UnauthorizedActionException("User cannot request adding admins to facility, user is not an admin");
		}

		Facility facility = perunConnector.getFacilityById(facilityId);
		if (facility == null) {
			log.error("Could not fetch facility for id: {}", facilityId);
			throw new InternalErrorException("Could not find facility for id: " + facilityId);
		}

		Map<String, String> adminCodeMap = generateCodesForAdmins(admins, facilityId);
		Map<String, String> adminLinkMap = generateLinksForAdmins(facilityId, adminCodeMap);
		int newAdminsNotificationsSent = sendAdminsNotifications(adminLinkMap, facility);

		boolean successful = (newAdminsNotificationsSent == admins.size());
		if (!successful) {
			log.error("Some operations failed - newAdminsNotificationsSent: {} out of {}",
					newAdminsNotificationsSent, admins.size());
		} else {
			log.info("Notifications sent to requested admins");
		}

		log.debug("addAdminsNotify returns: {}", successful);
		return successful;
	}

	@Override
	public boolean confirmAddAdmin(User user, String code)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, ConnectorException, InternalErrorException
	{
		log.debug("confirmAddAdmin({})", code);

		if (Utils.checkParamsInvalid(user, code)) {
			log.error("Wrong parameters passed: (user: {}, code: {})", user, code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject decrypted = decryptAddAdminCode(code);
		boolean isValid = (!isExpiredCode(decrypted) && validateCode(code));

		if (! isValid) {
			log.error("User trying to become admin with invalid code: {}", decrypted);
			throw new ExpiredCodeException("Code is invalid");
		}

		Long facilityId = decrypted.getLong(FACILITY_ID_KEY);
		boolean added = perunConnector.addFacilityAdmin(facilityId, user.getId());
		boolean deletedCode = requestManager.deleteUsedCode(code);
		boolean successful = (added && deletedCode);

		if (!successful) {
			log.error("some operations failed: added: {}, deletedCode: {}", added, deletedCode);
		} else {
			log.info("Admin added, code deleted");
		}

		log.debug("confirmAddAdmin returns: {}", successful);
		return added;
	}

	@Override
	public boolean rejectAddAdmin(User user, String code)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, MalformedCodeException,
			ExpiredCodeException, InternalErrorException
	{
		log.debug("rejectAddAdmin(user: {}, code: {})", user, code);

		if (Utils.checkParamsInvalid(user, code)) {
			log.error("Wrong parameters passed: (user: {}, code: {})", user, code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject decrypted = decryptAddAdminCode(code);
		boolean isValid = (!isExpiredCode(decrypted) && validateCode(code));

		if (! isValid) {
			log.error("User trying to become reject becoming with invalid code: {}", decrypted);
			throw new ExpiredCodeException("Code is invalid");
		}

		boolean deletedCode = requestManager.deleteUsedCode(code);

		log.debug("rejectAddAdmin() returns: {}", deletedCode);
		return deletedCode;
	}

	@Override
	public boolean validateCode(String code) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
			MalformedCodeException
	{
		log.trace("validateCode({})", code);

		if (Utils.checkParamsInvalid(code)) {
			log.error("Wrong parameters passed: (code: {})", code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		boolean isValid = (!isExpiredCode(decryptRequestCode(code)) && requestManager.validateCode(code));

		log.trace("validateCode() returns: {}", isValid);
		return isValid;
	}

	/* PRIVATE METHODS */

	private Request createRequest(Long facilityId, Long userId, List<PerunAttribute> attributes, RequestAction action)
			throws InternalErrorException, ActiveRequestExistsException
	{
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMapAttrs(attributes, appConfig);
		return createRequest(facilityId, userId, action, convertedAttributes);
	}

	private Request createRequest(Long facilityId, Long userId, RequestAction action, Map<String,PerunAttribute> attributes)
			throws InternalErrorException, ActiveRequestExistsException
	{
		log.trace("createRequest(facilityId: {}, userId: {}, action: {}, attributes: {})",
				facilityId, userId, action, attributes);

		if (Utils.checkParamsInvalid(facilityId, userId, action, attributes)) {
			log.error("Wrong parameters passed: (facility: {}, userId: {}, action: {}, attributes: {})",
					facilityId, userId, action, attributes);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Request request = new Request();
		request.setFacilityId(facilityId);
		request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
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

	private boolean isFacilityAdmin(Long facilityId, Long userId) throws ConnectorException {
		log.trace("isFacilityAdmin(facilityId: {}, userId: {})", facilityId, userId);

		if (Utils.checkParamsInvalid(facilityId, userId)) {
			log.error("Wrong parameters passed: (facility: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

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

		if (Utils.checkParamsInvalid(reqUserId, userId)) {
			log.error("Wrong parameters passed: (reqUserId: {}, userId: {})", reqUserId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		boolean res = reqUserId.equals(userId);

		log.debug("isAdminInRequest returns: {}", res);
		return res;
	}

	private PerunAttribute generateProxyIdentifierAttr() {
		log.trace("generateProxyIdentifierAttr()");

		String identifierAttrName = appConfig.getProxyIdentifierAttributeName();
		String value = appConfig.getProxyIdentifierAttributeValue();

		PerunAttribute identifierAttr = new PerunAttribute();
		identifierAttr.setFullName(identifierAttrName);
		identifierAttr.setValue(Collections.singletonList(value));

		log.trace("generateProxyIdentifierAttr() returns: {}", identifierAttr);
		return identifierAttr;
	}

	private JSONObject decryptRequestCode(String code) throws InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException, MalformedCodeException
	{
		log.trace("decryptRequestCode({})", code);

		if (Utils.checkParamsInvalid(code)) {
			log.error("Wrong parameters passed: (code: {})", code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Base64.Decoder b64dec = Base64.getUrlDecoder();

		cipher.init(Cipher.DECRYPT_MODE, appConfig.getSecret());
		byte[] decrypted = cipher.doFinal(b64dec.decode(code));

		String objInString = new String(decrypted);

		try {
			JSONObject decryptedAsJson = new JSONObject(objInString);

			log.trace("decryptRequestCode() returns: {}", decryptedAsJson);
			return decryptedAsJson;
		} catch (JSONException e) {
			throw new MalformedCodeException();
		}
	}

	private String createRequestCode(Long requestId, Long facilityId, String requestedMail)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException
	{
		log.trace("createRequestCode(requestId: {}, facilityId: {}, requestedMail: {})", requestId, facilityId, requestedMail);

		if (Utils.checkParamsInvalid(requestId, facilityId, requestedMail)) {
			log.error("Wrong parameters passed: (requestId: {}, facilityId: {}, requestedMail: {})",
					requestId, facilityId, requestedMail);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject object = new JSONObject();
		object.put(REQUEST_ID_KEY, requestId);
		object.put(FACILITY_ID_KEY, facilityId);
		object.put(CREATED_AT_KEY, LocalDateTime.now().toString());
		object.put(REQUESTED_MAIL_KEY, requestedMail);

		String strToEncrypt = object.toString();
		Base64.Encoder b64enc = Base64.getUrlEncoder();

		cipher.init(Cipher.ENCRYPT_MODE, appConfig.getSecret());
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

		String encoded = b64enc.encodeToString(encrypted);

		log.trace("createRequestCode() returns: {}", encoded);
		return encoded;
	}

	private JSONObject decryptAddAdminCode(String code)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, MalformedCodeException
	{
		log.trace("decryptAddAdminCode({})", code);

		if (Utils.checkParamsInvalid(code)) {
			log.error("Wrong parameters passed: (code: {})", code);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Base64.Decoder b64dec = Base64.getUrlDecoder();

		cipher.init(Cipher.DECRYPT_MODE, appConfig.getSecret());
		byte[] decrypted = cipher.doFinal(b64dec.decode(code));

		String objInString = new String(decrypted);

		try {
			JSONObject decryptedAsJson = new JSONObject(objInString);
			log.trace("decryptAddAdminCode() returns: {}", decryptedAsJson);
			return decryptedAsJson;
		} catch (JSONException e) {
			throw new MalformedCodeException();
		}
	}

	private String createAddAdminCode(Long facilityId, String requestedMail)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException
	{
		log.trace("createRequestCode(facilityId: {}, requestedMail: {})", facilityId, requestedMail);

		if (Utils.checkParamsInvalid(facilityId, requestedMail)) {
			log.error("Wrong parameters passed: (facilityId: {}, requestedMail: {})", facilityId, requestedMail);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JSONObject object = new JSONObject();
		object.put(FACILITY_ID_KEY, facilityId);
		object.put(CREATED_AT_KEY, LocalDateTime.now().toString());
		object.put(REQUESTED_MAIL_KEY, requestedMail);

		String strToEncrypt = object.toString();
		Base64.Encoder b64enc = Base64.getUrlEncoder();

		cipher.init(Cipher.ENCRYPT_MODE, appConfig.getSecret());
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

		String encoded = b64enc.encodeToString(encrypted);

		log.trace("createRequestCode() returns: {}", encoded);
		return encoded;
	}

	private boolean isExpiredCode(JSONObject codeInJson) {
		log.trace("isExpiredCode({})", codeInJson);

		if (Utils.checkParamsInvalid(codeInJson)) {
			log.error("Wrong parameters passed: (codeInJson: {})", codeInJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		long daysValidPeriod = appConfig.getConfirmationPeriodDays();
		long hoursValidPeriod = appConfig.getConfirmationPeriodHours();

		LocalDateTime createdAt = LocalDateTime.parse(codeInJson.getString(CREATED_AT_KEY));
		LocalDateTime validUntil = createdAt.plusDays(daysValidPeriod).plusHours(hoursValidPeriod);

		boolean isExpired = LocalDateTime.now().isAfter(validUntil);

		log.trace("isExpiredCode() returns: {}", isExpired);
		return isExpired;
	}

	private int sendAuthoritiesNotifications(Map<String, String> authsWithLinks, Request req) {
		log.trace("sendAuthoritiesNotifications(authsWithLinks: {}, req: {})", authsWithLinks, req);

		if (Utils.checkParamsInvalid(authsWithLinks, req)) {
			log.error("Wrong parameters passed: (authsWithLinks: {}, req: {})", authsWithLinks, req);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		int sent = 0;
		for (Map.Entry<String, String> entry: authsWithLinks.entrySet()) {
			String mailAuthority = entry.getKey();
			String mailLink = entry.getValue();
			boolean res = Mails.authoritiesApproveProductionTransferNotify(mailLink, req.getFacilityName(), mailAuthority, messagesProperties);
			if (res) {
				sent++;
			} else {
				log.error("Sending notification to authority FAILED - authority: {}, link: {}", mailAuthority, mailLink);
			}
		}

		log.trace("sendAuthoritiesNotifications() returns: {}", sent);
		return sent;
	}

	private int sendAdminsNotifications(Map<String, String> adminLinkMap, Facility facility) {
		log.trace("sendAdminsNotifications(adminLinkMap: {}, facility: {})", adminLinkMap, facility);

		if (Utils.checkParamsInvalid(adminLinkMap, facility)) {
			log.error("Wrong parameters passed: (adminLinkMap: {}, facility: {})", adminLinkMap, facility);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		int sent = 0;
		for (Map.Entry<String, String> entry: adminLinkMap.entrySet()) {
			String mailAdmin = entry.getKey();
			String mailLink = entry.getValue();
			boolean successful = Mails.adminAddRemoveNotify(entry.getValue(), facility.getName(), entry.getKey(),
					messagesProperties);
			if (successful) {
				sent++;
			} else {
				log.error("Sending notification to new admin FAILED - admin: {}, link: {}", mailAdmin, mailLink);
			}
		}

		log.trace("sendAdminsNotifications() returns: {}", sent);
		return sent;
	}

	private Map<String, PerunAttribute> filterNotNullAttributes(Facility facility) {
		log.trace("filterNotNullAttributes({})", facility);

		if (Utils.checkParamsInvalid(facility)) {
			log.error("Wrong parameters passed: (facility: {})", facility);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, PerunAttribute> filteredAttributes = new HashMap<>();
		for (Map.Entry<String, PerunAttribute> entry : facility.getAttrs().entrySet()) {
			if (entry.getValue().getValue() != null) {
				filteredAttributes.put(entry.getKey(), entry.getValue());
			}
		}

		log.trace("filterNotNullAttributes() returns: {}", filteredAttributes);
		return filteredAttributes;
	}

	private List<String> getAttrsToKeep(boolean isOidc) {
		List<String> keptAttrs = new ArrayList<>();

		keptAttrs.addAll(config.getServiceInputs().stream()
				.map(AttrInput::getName)
				.collect(Collectors.toList()));

		keptAttrs.addAll(config.getOrganizationInputs().stream()
				.map(AttrInput::getName)
				.collect(Collectors.toList()));

		keptAttrs.addAll(config.getMembershipInputs().stream()
				.map(AttrInput::getName)
				.collect(Collectors.toList()));

		if (isOidc) {
			keptAttrs.addAll(config.getOidcInputs()
					.stream()
					.map(AttrInput::getName)
					.collect(Collectors.toList())
			);
		} else {
			keptAttrs.addAll(config.getSamlInputs()
					.stream()
					.map(AttrInput::getName)
					.collect(Collectors.toList())
			);
		}

		return keptAttrs;
	}

	private Map<String, String> generateCodesForAuthorities(Request request, List<String> authorities)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InternalErrorException
	{
		log.trace("generateCodesForAuthorities(request: {}, authorities: {})", request, authorities);

		if (Utils.checkParamsInvalid(request, authorities)) {
			log.error("Wrong parameters passed: (request: {}, authorities: {})", request, authorities);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		List<String> codes = new ArrayList<>();
		Map<String, String> authsCodesMap = new HashMap<>();

		for (String authority : authorities) {
			String code = createRequestCode(request.getReqId(), request.getFacilityId(), authority);
			codes.add(code);
			authsCodesMap.put(authority, code);
		}

		requestManager.storeCodes(codes);

		log.trace("generateCodesForAuthorities() returns: {}", authsCodesMap);
		return authsCodesMap;
	}

	private Map<String, String> generateCodesForAdmins(List<String> admins, Long facilityId)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InternalErrorException
	{
		log.trace("generateCodesForAdmins(facilityId: {}, admins: {})", facilityId, admins);

		if (Utils.checkParamsInvalid(admins, facilityId)) {
			log.error("Wrong parameters passed: (admins: {}, facilityId: {})", admins, facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		List<String> codes = new ArrayList<>();
		Map<String, String> adminCodesMap = new HashMap<>();

		for (String admin : admins) {
			String code = createAddAdminCode(facilityId, admin);
			codes.add(code);
			adminCodesMap.put(admin, code);
		}

		requestManager.storeCodes(codes);

		log.trace("generateCodesForAdmins() returns: {}", adminCodesMap);
		return adminCodesMap;
	}

	private Map<String, String> generateLinksForAdmins(Long facilityId, Map<String, String> adminCodeMap)
			throws ConnectorException, UnsupportedEncodingException
	{
		log.trace("generateLinksForAdmins(adminCodeMap: {}, facilityId: {})", adminCodeMap, facilityId);

		if (Utils.checkParamsInvalid(facilityId, adminCodeMap)) {
			log.error("Wrong parameters passed: (facilityId: {}, adminCodeMap: {})", facilityId, adminCodeMap);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Facility facility = perunConnector.getFacilityById(facilityId);
		Map<String, String> linksMap = new HashMap<>();

		for (Map.Entry<String, String> entry : adminCodeMap.entrySet()) {
			String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
			String link = appConfig.getAdminsEndpoint()
					.concat("?facilityName=").concat(facility.getName())
					.concat("&code=").concat(code);
			linksMap.put(entry.getKey(), link);
			log.debug("Generated code: {}", code); //TODO: remove
		}

		log.trace("generateLinksForAdmins() returns: {}", linksMap);
		return linksMap;
	}

	private Map<String, String> generateLinksForAuthorities(Map<String, String> authorityCodeMap, Request request)
			throws UnsupportedEncodingException, ConnectorException
	{
		log.trace("generateLinksForAuthorities(authorityCodeMap: {}, request: {})", authorityCodeMap, request);

		if (Utils.checkParamsInvalid(authorityCodeMap, request)) {
			log.error("Wrong parameters passed: (authorityCodeMap: {}, request: {})", authorityCodeMap, request);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Facility facility = perunConnector.getFacilityById(request.getFacilityId());
		Map<String, String> linksMap = new HashMap<>();

		for (Map.Entry<String, String> entry : authorityCodeMap.entrySet()) {
			String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
			String link = appConfig.getSignaturesEndpointUrl()
					.concat("?facilityName=").concat(facility.getName())
					.concat("&code=").concat(code);
			linksMap.put(entry.getKey(), link);
			log.debug("Generated code: {}", code); //TODO: remove
		}

		log.trace("generateLinksForAuthorities() returns: {}", linksMap);
		return linksMap;
	}
}
