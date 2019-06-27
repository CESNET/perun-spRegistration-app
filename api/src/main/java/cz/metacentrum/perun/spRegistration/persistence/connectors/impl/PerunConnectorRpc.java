package cz.metacentrum.perun.spRegistration.persistence.connectors.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.spRegistration.persistence.Utils;
import cz.metacentrum.perun.spRegistration.persistence.connectors.ConnectorUtils;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.json.JSONArray;
import org.json.JSONObject;
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
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
public class PerunConnectorRpc implements PerunConnector {

	private static final Logger log = LoggerFactory.getLogger(PerunConnectorRpc.class);

	private String perunRpcUrl;
	private String perunRpcUser;
	private String perunRpcPassword;

	private static final String USERS_MANAGER = "usersManager";
	private static final String FACILITIES_MANAGER = "facilitiesManager";
	private static final String ATTRIBUTES_MANAGER = "attributesManager";
	private static final String SEARCHER = "searcher";

	public void setPerunRpcUrl(String perunRpcUrl) {
		this.perunRpcUrl = perunRpcUrl;
	}

	public void setPerunRpcUser(String perunRpcUser) {
		this.perunRpcUser = perunRpcUser;
	}

	public void setPerunRpcPassword(String perunRpcPassword) {
		this.perunRpcPassword = perunRpcPassword;
	}

	@Override
	public Facility createFacilityInPerun(JSONObject facilityJson) throws ConnectorException {
		log.trace("createFacilityInPerun({})", facilityJson);
		if (facilityJson == null) {
			throw new IllegalArgumentException("facilityJson is null");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		JSONObject res = new JSONObject(makeRpcPostCall(FACILITIES_MANAGER, "createFacility", params));
		Facility facility = MapperUtils.mapFacility(res);

		log.trace("createFacilityInPerun() returns: {}", facility);
		return facility;
	}

	@Override
	public Facility updateFacilityInPerun(JSONObject facilityJson) throws ConnectorException {
		log.trace("updateFacilityInPerun({})", facilityJson);
		if (facilityJson == null) {
			throw new IllegalArgumentException("facilityJson is null");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		JSONObject res = new JSONObject(makeRpcPostCall(FACILITIES_MANAGER, "updateFacility", params));
		Facility facility = MapperUtils.mapFacility(res);

		log.trace("updateFacilityInPerun() returns: {}", facility);
		return facility;
	}

	@Override
	public boolean deleteFacilityFromPerun(Long facilityId) throws ConnectorException {
		log.trace("deleteFacilityFromPerun({})", facilityId);
		if (facilityId == null) {
			throw new IllegalArgumentException("facilityId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);

		boolean successful = null != makeRpcPostCall(FACILITIES_MANAGER, "deleteFacility", params);

		log.trace("deleteFacilityFromPerun() returns: {}", successful);
		return successful;
	}

	@Override
	public Facility getFacilityById(Long facilityId) throws ConnectorException {
		log.trace("getFacilityById({})", facilityId);
		if (facilityId == null) {
			throw new IllegalArgumentException("facilityId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("id", facilityId);

		JSONObject res = makeRpcGetCallForObject(FACILITIES_MANAGER, "getFacilityById", params);
		Facility facility = MapperUtils.mapFacility(res);

		log.trace("getFacilityById() returns: {}", facility);
		return facility;
	}

	@Override
	public List<Facility> getFacilitiesByProxyIdentifier(String proxyIdentifierAttr, String proxyIdentifier) throws ConnectorException {
		log.trace("getFacilitiesByProxyIdentifier(proxyIdentifierAttr: {}, proxyIdentifier: {})",
				proxyIdentifierAttr, proxyIdentifier);
		if (proxyIdentifierAttr == null || proxyIdentifier == null) {
			throw new IllegalArgumentException("Illegal input: proxyIdentifierAttr: " + proxyIdentifierAttr +
					", proxyIdentifier: " + proxyIdentifier);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		Map<String, String> attributesWithSearchingValues = new HashMap<>();
		attributesWithSearchingValues.put(proxyIdentifierAttr, proxyIdentifier);
		params.put("attributesWithSearchingValues", attributesWithSearchingValues);

		JSONArray res = new JSONArray(makeRpcPostCall(SEARCHER, "getFacilities", params));
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByProxyIdentifier() returns: {}", facilities);
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException {
		log.trace("getFacilitiesWhereUserIsAdmin({})", userId);
		if (userId == null) {
			throw new IllegalArgumentException("userId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("user", userId);

		JSONArray res = makeRpcGetCallForArray(FACILITIES_MANAGER, "getFacilitiesWhereUserIsAdmin", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesWhereUserIsAdmin() returns: {}", facilities);
		return facilities;
	}

	@Override
	public PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws ConnectorException {
		log.trace("getFacilityAttribute(facilityId: {}, attrName: {})", facilityId, attrName);
		if (facilityId == null || attrName == null) {
			throw new IllegalArgumentException("Illegal input: facilityId: " + facilityId + ", attrName: " + attrName);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributeName", attrName);

		JSONObject res = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params);
		PerunAttribute attribute = MapperUtils.mapAttribute(res);

		log.trace("getFacilityAttribute() returns: {}", attribute);
		return attribute;
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws ConnectorException {
		log.trace("getFacilityAttributes(facilityId: {}, attrNames: {})", facilityId, attrNames);
		if (facilityId == null || attrNames == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrNames" + attrNames);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attrNames", attrNames);

		JSONArray res = makeRpcGetCallForArray(ATTRIBUTES_MANAGER, "getAttributes", params);
		Map<String, PerunAttribute> attributeMap = MapperUtils.mapAttributes(res);

		log.trace("getFacilityAttributes() returns: {}", attributeMap);
		return attributeMap;
	}

	@Override
	public boolean setFacilityAttribute(Long facilityId, JSONObject attrJson) throws ConnectorException {
		log.trace("setFacilityAttribute(facilityId: {}, attrJson: {})", facilityId, attrJson);
		if (facilityId == null || attrJson == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrJson" + attrJson);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attribute", attrJson);

		boolean successful = null == makeRpcPostCall(ATTRIBUTES_MANAGER, "setAttribute", params);

		log.trace("setFacilityAttribute() returns: {}", successful);
		return successful;
	}

	@Override
	public boolean setFacilityAttributes(Long facilityId, JSONArray attrsJsons) throws ConnectorException {
		log.trace("setFacilityAttributes(facilityId: {}, attrsJsons: {})", facilityId, attrsJsons);
		if (facilityId == null || attrsJsons == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrJsons" + attrsJsons);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributes", attrsJsons);

		boolean successful = null == makeRpcPostCall(ATTRIBUTES_MANAGER, "setAttributes", params);

		log.trace("setFacilityAttributes() returns: {}", successful);
		return successful;
	}

	@Override
	public User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws ConnectorException {
		log.trace("getUserWithEmail({})", extLogin);
		if (extLogin == null || extLogin.isEmpty()) {
			throw new IllegalArgumentException("userId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();

		params.put("extSourceName", extSourceName);
		params.put("extLogin", extLogin);

		JSONObject res = makeRpcGetCallForObject(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", params);
		if (res == null) {
			throw new ConnectorException("Should not found more than one user");
		}

		User user = MapperUtils.mapUser(res, false);
		if (user != null) {
			params.clear();
			params.put("user", user.getId().intValue());
			params.put("attributeName", userEmailAttr);

			JSONObject attr = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params);
			PerunAttribute attribute = MapperUtils.mapAttribute(attr);
			if (attribute != null) {
				user.setEmail(attribute.valueAsString());
			}
		}

		log.trace("getUserWithEmail() returns: {}", user);
		return user;
	}

	@Override
	public boolean addFacilityAdmin(Long facilityId, Long userId) throws ConnectorException {
		log.trace("addFacilityAdmin(facilityId: {}, userId:{})", facilityId, userId);
		if (facilityId == null || userId == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("user", userId);

		boolean res = null == makeRpcPostCall(FACILITIES_MANAGER, "addAdmin", params);

		log.trace("addFacilityAdmin() returns: {}", res);
		return res;
	}

	@Override
	public Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws ConnectorException {
		log.trace("getFacilityIdsWhereUserIsAdmin({})", userId);
		if (userId == null) {
			throw new IllegalArgumentException("userId is null");
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
		if (attributeName == null) {
			throw new IllegalArgumentException("attributeName is null");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", attributeName);

		JSONObject res = makeRpcGetCallForObject(ATTRIBUTES_MANAGER, "getAttributeDefinition", params);
		PerunAttributeDefinition definition = MapperUtils.mapAttrDefinition(res);

		log.trace("getAttributeDefinition() returns: {}", definition);
		return definition;
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(String attrName, String attrValue) throws ConnectorException {
		log.trace("getFacilitiesByAttribute(attrName: {}, attrValue: {})", attrName, attrName);
		if (attrName == null) {
			throw new IllegalArgumentException("attrName is null");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", attrName);
		params.put("attributeValue", attrValue);

		JSONArray res = makeRpcGetCallForArray(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByAttribute() returns: {}", facilities);
		return facilities;
	}

	private JSONObject makeRpcGetCallForObject(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcGetCallForObject(manager: {}, method: {}, map: {}", manager, method, map);
		String response = makeRpcGetCall(manager, method, map);
		if (response == null || response.equalsIgnoreCase("null")) {
			return null;
		}

		JSONObject result = new JSONObject(response);

		log.trace("makeRpcCallForObject() returns: {}",result);
		return result;
	}

	private JSONArray makeRpcGetCallForArray(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcCallForArray(manager: {}, method: {}, map: {}", manager, method, map);
		String response = makeRpcGetCall(manager, method, map);
		if (response == null || response.equalsIgnoreCase("null")) {
			return null;
		}
		JSONArray result = new JSONArray(response);

		log.trace("makeRpcGetCallForArray() returns: {}",result);
		return result;
	}

	private String makeRpcGetCall(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcGetCall(manager: {}, method: {}, map: {})", manager, method, map);
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

			String result = (response != null) ? Utils.prettyPrintJsonString(response) : null;
			log.trace("makeRpcGetCall() returns: {}", result);
			return result;
		} catch (HttpClientErrorException ex) {
			return ConnectorUtils.dealWithHttpClientErrorException(ex, "Could not connect to Perun RPC");
		} catch (IOException e) {
			log.error("cannot parse response to String", e);
			throw new ConnectorException("cannot connect to Perun RPC", e);
		}
	}

	private String makeRpcPostCall(String manager, String method, Map<String, Object> map) throws ConnectorException {
		log.trace("makeRpcPostCall(manager: {}, method: {}, params: {})", manager, method, map);
		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors =
				Collections.singletonList(new BasicAuthorizationInterceptor(perunRpcUser, perunRpcPassword));
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
		String actionUrl = perunRpcUrl + "/json/" + manager + '/' + method;

		try {
			HttpEntity<byte[]> entity = prepareJsonBody(map);
			JsonNode response = restTemplate.postForObject(actionUrl, entity, JsonNode.class);

			String result = (response != null) ? Utils.prettyPrintJsonString(response) : null;
			log.trace("makeRpcPostCall() returns: {}", result);
			return result;
		} catch (HttpClientErrorException ex) {
			return ConnectorUtils.dealWithHttpClientErrorException(ex, "Could not connect to Perun RPC");
		} catch (IOException e) {
			log.error("cannot parse response to String", e);
			throw new ConnectorException("cannot connect to Perun RPC", e);
		}
	}

	private HttpEntity<byte[]> prepareJsonBody(Map<String, Object> map) {
		log.trace("prepareJsonBody({})", map);
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<byte[]> result = new HttpEntity<>(StandardCharsets.UTF_8.encode(obj.toString()).array(), headers);
		log.trace("prepareJsonBody() returns: {}", result);
		return result;
	}

}
