package cz.metacentrum.perun.spRegistration.persistence.connectors.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.exceptions.BadRequestException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun via RPC.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class PerunConnectorRpc implements PerunConnector {

	private static final Logger log = LoggerFactory.getLogger(PerunConnectorRpc.class);

	private static final String USERS_MANAGER = "usersManager";
	private static final String FACILITIES_MANAGER = "facilitiesManager";
	private static final String ATTRIBUTES_MANAGER = "attributesManager";
	private static final String SEARCHER = "searcher";
	private static final String GROUPS_MANAGER = "groupsManager";
	private static final String MEMBERS_MANAGER = "membersManager";

	private String perunRpcUrl;
	private String perunRpcUser;
	private String perunRpcPassword;
	private String userEmailAttr;
	private final Set<Long> appAdminIds = new HashSet<>();

	public void setPerunRpcUrl(String perunRpcUrl) {
		this.perunRpcUrl = perunRpcUrl;
	}

	public void setPerunRpcUser(String perunRpcUser) {
		this.perunRpcUser = perunRpcUser;
	}

	public void setPerunRpcPassword(String perunRpcPassword) {
		this.perunRpcPassword = perunRpcPassword;
	}

	public void setUserEmailAttr(String userEmailAttr) {
		this.userEmailAttr = userEmailAttr;
	}

	public void setAppAdminIds(String[] appAdminIdsStrs) {
		for (String appAdminIdStr: appAdminIdsStrs) {
			appAdminIds.add(Long.getLong(appAdminIdStr));
		}
	}

	@Override
	public Facility createFacilityInPerun(JsonNode facilityJson) throws ConnectorException, BadRequestException {
		log.trace("createFacilityInPerun({})", facilityJson);

		if (Utils.checkParamsInvalid(facilityJson)) {
			log.error("Wrong parameters passed: (facilityJson: {})", facilityJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		try {
			JsonNode res = makeRpcPostCall(FACILITIES_MANAGER, "createFacility", params);
			Facility facility = MapperUtils.mapFacility(res);

			log.trace("createFacilityInPerun() returns: {}", facility);
			return facility;
		} catch (ConnectorException e) {
			if (e.getCause() != null && e.getCause() instanceof HttpClientErrorException) {
				throw new BadRequestException();
			}

			throw e;
		}
	}

	@Override
	public Facility updateFacilityInPerun(JsonNode facilityJson) throws ConnectorException, BadRequestException {
		log.trace("updateFacilityInPerun({})", facilityJson);

		if (Utils.checkParamsInvalid(facilityJson)) {
			log.error("Wrong parameters passed: (facilityJson: {})", facilityJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		try {
			JsonNode res = makeRpcPostCall(FACILITIES_MANAGER, "updateFacility", params);
			Facility facility = MapperUtils.mapFacility(res);

			log.trace("updateFacilityInPerun() returns: {}", facility);
			return facility;
		} catch (ConnectorException e) {
			if (e.getCause() != null && e.getCause() instanceof HttpClientErrorException) {
				throw new BadRequestException();
			}

			throw e;
		}
	}

	@Override
	public boolean deleteFacilityFromPerun(Long facilityId) throws ConnectorException {
		log.trace("deleteFacilityFromPerun({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("force", true);

		boolean successful = null != makeRpcPostCall(FACILITIES_MANAGER, "deleteFacility", params);

		log.trace("deleteFacilityFromPerun() returns: {}", successful);
		return successful;
	}

	@Override
	public Facility getFacilityById(Long facilityId) throws ConnectorException {
		log.trace("getFacilityById({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("id", facilityId);

		JsonNode res = makeRpcGetCallForObject(FACILITIES_MANAGER, "getFacilityById", params);
		Facility facility = MapperUtils.mapFacility(res);

		List<User> admins = getAdminsForFacility(facilityId, userEmailAttr);
		facility.setAdmins(admins);

		log.trace("getFacilityById() returns: {}", facility);
		return facility;
	}

	@Override
	public List<Facility> getFacilitiesByProxyIdentifier(String proxyIdentifierAttr, String proxyIdentifier)
			throws ConnectorException
	{
		log.trace("getFacilitiesByProxyIdentifier(proxyIdentifierAttr: {}, proxyIdentifier: {})",
				proxyIdentifierAttr, proxyIdentifier);

		if (Utils.checkParamsInvalid(proxyIdentifierAttr, proxyIdentifier)) {
			log.error("Wrong parameters passed: (proxyIdentifierAttr: {}, proxyIdentifier: {})",
					proxyIdentifierAttr, proxyIdentifier);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		Map<String, String> attributesWithSearchingValues = new HashMap<>();
		attributesWithSearchingValues.put(proxyIdentifierAttr, proxyIdentifier);
		params.put("attributesWithSearchingValues", attributesWithSearchingValues);

		JsonNode res = makeRpcPostCall(SEARCHER, "getFacilities", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByProxyIdentifier() returns: {}", facilities);
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException {
		log.trace("getFacilitiesWhereUserIsAdmin({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("user", userId);

		JsonNode res = makeRpcGetCallForArray(FACILITIES_MANAGER, "getFacilitiesWhereUserIsAdmin", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesWhereUserIsAdmin() returns: {}", facilities);
		return facilities;
	}

	@Override
	public PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws ConnectorException {
		log.trace("getFacilityAttribute(facilityId: {}, attrName: {})", facilityId, attrName);

		if (Utils.checkParamsInvalid(facilityId, attrName)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrName: {})", facilityId, attrName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributeName", attrName);

		JsonNode res = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params);
		PerunAttribute attribute = MapperUtils.mapAttribute(res);

		log.trace("getFacilityAttribute() returns: {}", attribute);
		return attribute;
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws ConnectorException {
		log.trace("getFacilityAttributes(facilityId: {}, attrNames: {})", facilityId, attrNames);

		if (Utils.checkParamsInvalid(facilityId, attrNames)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrNames: {})", facilityId, attrNames);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attrNames", attrNames);

		JsonNode res = makeRpcGetCallForArray(ATTRIBUTES_MANAGER, "getAttributes", params);
		Map<String, PerunAttribute> attributeMap = MapperUtils.mapAttributes(res);

		log.trace("getFacilityAttributes() returns: {}", attributeMap);
		return attributeMap;
	}

	@Override
	public boolean setFacilityAttribute(Long facilityId, JsonNode attrJson) throws ConnectorException {
		log.trace("setFacilityAttribute(facilityId: {}, attrJson: {})", facilityId, attrJson);

		if (Utils.checkParamsInvalid(facilityId, attrJson)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrJson: {})", facilityId, attrJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attribute", attrJson);

		boolean successful = (null != makeRpcPostCall(ATTRIBUTES_MANAGER, "setAttribute", params));

		log.trace("setFacilityAttribute() returns: {}", successful);
		return successful;
	}

	@Override
	public boolean setFacilityAttributes(Long facilityId, JsonNode attrsJsons) throws ConnectorException {
		log.trace("setFacilityAttributes(facilityId: {}, attrsJsons: {})", facilityId, attrsJsons);

		if (Utils.checkParamsInvalid(facilityId, attrsJsons)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributes", attrsJsons);

		boolean successful = (null == makeRpcPostCall(ATTRIBUTES_MANAGER, "setAttributes", params));

		log.trace("setFacilityAttributes() returns: {}", successful);
		return successful;
	}

	@Override
	public User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws ConnectorException {
		log.trace("getUserWithEmail({})", extLogin);

		if (Utils.checkParamsInvalid(extLogin, extSourceName, userEmailAttr)) {
			log.error("Wrong parameters passed: (extLogin: {}, extSourceName: {}, userEmailAttr: {})",
					extLogin, extSourceName, userEmailAttr);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();

		params.put("extSourceName", extSourceName);
		params.put("extLogin", extLogin);

		JsonNode res = makeRpcGetCallForObject(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", params);
		if (res == null || res.isNull()) {
			throw new ConnectorException("Should not found more than one user");
		}

		User user = MapperUtils.mapUser(res);
		params.clear();
		params.put("user", user.getId().intValue());
		params.put("attributeName", userEmailAttr);

		JsonNode attr = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params);
		PerunAttribute attribute = MapperUtils.mapAttribute(attr);
		user.setEmail(attribute.valueAsString());

		log.trace("getUserWithEmail() returns: {}", user);
		return user;
	}

	@Override
	public Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws ConnectorException {
		log.trace("getFacilityIdsWhereUserIsAdmin({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		List<Facility> facilities = getFacilitiesWhereUserIsAdmin(userId);
		if (facilities == null) {
			return new HashSet<>();
		}

		Set<Long> ids = new HashSet<>();
		facilities.forEach(f -> ids.add(f.getId()));

		log.trace("getFacilityIdsWhereUserIsAdmin() returns: {}", ids);
		return ids;
	}

	@Override
	public PerunAttributeDefinition getAttributeDefinition(String attributeName) throws ConnectorException {
		log.trace("getAttributeDefinition({})", attributeName);

		if (Utils.checkParamsInvalid(attributeName)) {
			log.error("Wrong parameters passed: (attributeName: {})", attributeName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", attributeName);

		JsonNode res = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttributeDefinition", params);
		PerunAttributeDefinition definition = MapperUtils.mapAttrDefinition(res);

		log.trace("getAttributeDefinition() returns: {}", definition);
		return definition;
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(String attrName, String attrValue) throws ConnectorException {
		log.trace("getFacilitiesByAttribute(attrName: {}, attrValue: {})", attrName, attrName);

		if (Utils.checkParamsInvalid(attrName)) {
			log.error("Wrong parameters passed: (attrName: {})", attrName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", attrName);
		params.put("attributeValue", attrValue);

		JsonNode res = makeRpcGetCallForArray(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByAttribute() returns: {}", facilities);
		return facilities;
	}

	@Override
	public User getUserById(Long id) throws ConnectorException {
		log.trace("getUserById({})", id);

		if (Utils.checkParamsInvalid(id)) {
			log.error("Wrong parameters passed: (id: {})", id);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("id", id);

		JsonNode res = makeRpcGetCallForObject(USERS_MANAGER, "getUserById", params);
		User user = MapperUtils.mapUser(res);

		log.trace("getUserById() returns: {}", user);
		return user;
	}

	@Override
	public Group createGroup(Long parentGroupId, Group group) throws ConnectorException {
		log.trace("createGroup({}, {})", parentGroupId, group);

		if (Utils.checkParamsInvalid(parentGroupId, group)) {
			log.error("Wrong parameters passed: (parentGroupId: {}, group: {})", parentGroupId, group);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("parentGroup", parentGroupId);
		params.put("group", group.toJson());

		JsonNode res = makeRpcGetCall(GROUPS_MANAGER, "createGroup", params);
		Group g = MapperUtils.mapGroup(res);

		log.trace("createGroup({}, {}) returns: {}", parentGroupId, group, g);
		return g;
	}

	@Override
	public boolean deleteGroup(Long groupId) throws ConnectorException {
		log.trace("deleteGroup({})", groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("force", true);

		JsonNode res = makeRpcGetCall(GROUPS_MANAGER, "deleteGroup", params);
		boolean result = res == null || res.isNull();

		log.trace("deleteGroup({}) returns: {}", groupId, result);
		return result;
	}

	@Override
	public boolean addGroupAsAdmins(Long facilityId, Long groupId) throws ConnectorException {
		log.trace("addGroupAsAdmins({}, {})", facilityId, groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = makeRpcGetCall(FACILITIES_MANAGER, "addAdmin", params);
		boolean result = res == null || res.isNull();

		log.trace("addGroupAsAdmins({}, {}) returns: {}", facilityId, groupId, result);
		return result;
	}

	@Override
	public boolean removeGroupFromAdmins(Long facilityId, Long groupId) throws ConnectorException {
		log.trace("removeGroupFromAdmins({}, {})", facilityId, groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = makeRpcGetCall(FACILITIES_MANAGER, "removeAdmin", params);
		boolean result = res == null || res.isNull();

		log.trace("removeGroupFromAdmins({}, {}) returns: {}", facilityId, groupId, result);
		return result;
	}

	@Override
	public Long getMemberIdByUser(Long vo, Long user) throws ConnectorException {
		log.trace("getMemberIdByUser({}, {})", vo, user);

		if (Utils.checkParamsInvalid(vo, user)) {
			log.error("Wrong parameters passed: (vo: {}, user: {})", vo, user);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("vo", vo);
		params.put("user", user);

		JsonNode res = makeRpcGetCall(MEMBERS_MANAGER, "getMemberByUser", params);
		Long id = res.get("id").asLong();

		log.trace("getMemberIdByUser({}, {}) returns: {}", vo, user, id);
		return id;
	}

	@Override
	public boolean addMemberToGroup(Long groupId, Long memberId) throws ConnectorException {
		log.trace("addMemberToGroup({}, {})", groupId, memberId);

		if (Utils.checkParamsInvalid(groupId, memberId)) {
			log.error("Wrong parameters passed: (groupId: {}, memberId: {})", groupId, memberId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("member", memberId);

		JsonNode res = makeRpcGetCall(GROUPS_MANAGER, "addMember", params);
		boolean result = res == null || res.isNull();

		log.trace("addMemberToGroup({}, {}) returns: {}", groupId, memberId, result);
		return result;
	}

	/* PRIVATE METHODS */

	private List<User> getAdminsForFacility(Long facility, String userEmailAttr) throws ConnectorException {
		log.trace("getAdminsForFacility({})", facility);
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facility);
		params.put("specificAttributes", Collections.singletonList(userEmailAttr));
		params.put("allUserAttributes", false);
		params.put("onlyDirectAdmins", false);

		JsonNode res = makeRpcGetCallForArray(FACILITIES_MANAGER, "getRichAdmins", params);
		List<User> admins = MapperUtils.mapUsers(res, userEmailAttr);
		for (User u: admins) {
			u.setAdmin(appAdminIds.contains(u.getId()));
		}

		log.trace("getAdminsForFacility() returns: {}", admins);
		return admins;
	}

	private JsonNode makeRpcGetCallForObject(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcGetCallForObject(manager: {}, method: {}, map: {}", manager, method, map);

		if (Utils.checkParamsInvalid(manager, method)) {
			log.error("Wrong parameters passed: (manager: {}, method: {})", manager, method);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JsonNode response = makeRpcGetCall(manager, method, map);
		if (response == null || response.isNull()) {
			return null;
		}

		log.trace("makeRpcCallForObject() returns: {}", response);
		return response;
	}

	private JsonNode makeRpcGetCallForArray(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcCallForArray(manager: {}, method: {}, map: {}", manager, method, map);

		if (Utils.checkParamsInvalid(manager, method)) {
			log.error("Wrong parameters passed: (manager: {}, method: {})", manager, method);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		JsonNode response = makeRpcGetCall(manager, method, map);
		if (response == null || response.isNull()) {
			return null;
		}

		log.trace("makeRpcGetCallForArray() returns: {}", response);
		return response;
	}

	private JsonNode makeRpcGetCall(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcGetCall(manager: {}, method: {}, map: {})", manager, method, map);

		if (Utils.checkParamsInvalid(manager, method)) {
			log.error("Wrong parameters passed: (manager: {}, method: {})", manager, method);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		//prepare basic auth
		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors =
				Collections.singletonList(new BasicAuthorizationInterceptor(perunRpcUser, perunRpcPassword));
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
		String actionUrl = perunRpcUrl + "/json/" + manager + '/' + method;

		try {
			//we will use post as perun has a complicated way to use get requests...
			//sending post will always succeed and deliver the parameters
			JsonNode response = restTemplate.postForObject(actionUrl, map, JsonNode.class);
			log.trace("makeRpcGetCall() returns: {}", response);
			return response;
		} catch (HttpClientErrorException ex) {
			return dealWithHttpClientErrorException(ex, "Could not connect to Perun RPC");
		}
	}

	private JsonNode makeRpcPostCall(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcPostCall(manager: {}, method: {}, params: {})", manager, method, map);

		if (Utils.checkParamsInvalid(manager, method)) {
			log.error("Wrong parameters passed: (manager: {}, method: {})", manager, method);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors =
				Collections.singletonList(new BasicAuthorizationInterceptor(perunRpcUser, perunRpcPassword));
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
		String actionUrl = perunRpcUrl + "/json/" + manager + '/' + method;

		try {
			HttpEntity<byte[]> entity = prepareJsonBody(map);
			JsonNode response = restTemplate.postForObject(actionUrl, entity, JsonNode.class);

			log.trace("makeRpcPostCall() returns: {}", response);
			return response;
		} catch (HttpClientErrorException ex) {
			return dealWithHttpClientErrorException(ex, "Could not connect to Perun RPC");
		} catch (IOException e) {
			log.error("cannot parse response to String", e);
			throw new ConnectorException("cannot connect to Perun RPC", e);
		}
	}

	private HttpEntity<byte[]> prepareJsonBody(Map<String, Object> map) throws JsonProcessingException {
		log.trace("prepareJsonBody({})", map);

		if (Utils.checkParamsInvalid(map)) {
			log.error("Wrong parameters passed: (map: {})", map);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String body = new ObjectMapper().writeValueAsString(map);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<byte[]> result = new HttpEntity<>(StandardCharsets.UTF_8.encode(body).array(), headers);
		log.trace("prepareJsonBody() returns: {}", result);
		return result;
	}

	public JsonNode dealWithHttpClientErrorException(HttpClientErrorException ex, String message) throws ConnectorException {
		log.trace("dealWithHttpClientErrorException(ex: {}, message: {})", ex, message);

		MediaType contentType = null;
		if (ex.getResponseHeaders() != null) {
			contentType = ex.getResponseHeaders().getContentType();
		}
		if (contentType != null && "json".equalsIgnoreCase(contentType.getSubtype())) {
			try {
				String body = ex.getResponseBodyAsString();
				JsonNode error = new ObjectMapper().readValue(body, JsonNode.class);
				String exErrorId = error.path("errorId").textValue();
				String exName = error.path("name").textValue();
				String exMessage = error.path("message").textValue();

				String errMessage = "Error from Perun: { id: " + exErrorId + ", name: " + exName + ", message: " + exMessage + " }";
				throw new ConnectorException(errMessage, ex);
			} catch (IOException e) {
				throw new ConnectorException("Perun RPC Exception thrown, cannot read message");
			}
		} else {
			throw new ConnectorException("Perun RPC Exception thrown, cannot read message");
		}
	}

}
