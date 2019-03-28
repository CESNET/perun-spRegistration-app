package cz.metacentrum.perun.spRegistration.persistence.rpc.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun via RPC.
 *
 * @author Dominik František Bučík bucik@ics.muni.cz
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
	public Facility createFacilityInPerun(JSONObject facilityJson) throws RPCException {
		log.debug("createFacilityInPerun({})", facilityJson);
		if (facilityJson == null) {
			throw new IllegalArgumentException("facilityJson is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		JSONObject res = makeRpcCallForObject(FACILITIES_MANAGER, "createFacility", params, true);
		Facility facility = MapperUtils.mapFacility(res);

		log.debug("createFacilityInPerun returns: {}", facility);
		return facility;
	}

	@Override
	public Facility updateFacilityInPerun(JSONObject facilityJson) throws RPCException {
		log.debug("updateFacilityInPerun({})", facilityJson);
		if (facilityJson == null) {
			throw new IllegalArgumentException("facilityJson is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityJson);

		JSONObject res = makeRpcCallForObject(FACILITIES_MANAGER, "updateFacility", params, true);
		Facility facility = MapperUtils.mapFacility(res);

		log.debug("updateFacilityInPerun returns: {}", facility);
		return facility;
	}

	@Override
	public boolean deleteFacilityFromPerun(Long facilityId) throws RPCException {
		log.debug("deleteFacilityFromPerun({})", facilityId);
		if (facilityId == null) {
			throw new IllegalArgumentException("facilityId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);

		makeRpcCallForObject(FACILITIES_MANAGER, "deleteFacility", params, false);

		log.debug("deleteFacilityFromPerun returns: {}", true);
		return true;
	}

	@Override
	public Facility getFacilityById(Long facilityId) throws RPCException {
		log.debug("getFacilityById({})", facilityId);
		if (facilityId == null) {
			throw new IllegalArgumentException("facilityId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("id", facilityId);

		JSONObject res = makeRpcCallForObject(FACILITIES_MANAGER, "getFacilityById", params, false);
		Facility facility = MapperUtils.mapFacility(res);

		log.debug("getFacilityById returns: {}", facility);
		return facility;
	}

	@Override
	public List<Facility> getFacilitiesViaSearcher(Map<String, String> attributesWithSearchingValues) throws RPCException {
		log.debug("getFacilitiesViaSearcher({})", attributesWithSearchingValues);
		if (attributesWithSearchingValues == null) {
			throw new IllegalArgumentException("attributesWithSearchingValues is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributesWithSearchingValues", attributesWithSearchingValues);

		JSONArray res = makeRpcCallForArray(SEARCHER, "getFacilities", params, false);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.debug("getFacilitiesViaSearcher returns: {}", facilities);
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws RPCException {
		log.debug("getFacilitiesWhereUserIsAdmin({})", userId);
		if (userId == null) {
			throw new IllegalArgumentException("userId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("user", userId);

		JSONArray res = makeRpcCallForArray(FACILITIES_MANAGER, "getFacilitiesWhereUserIsAdmin", params, false);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.debug("getFacilitiesWhereUserIsAdmin returns: {}", facilities);
		return facilities;
	}

	@Override
	public PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws RPCException {
		log.debug("getFacilityAttribute(facilityId: {}, attrName: {})", facilityId, attrName);
		if (facilityId == null || attrName == null) {
			throw new IllegalArgumentException("Illegal input: facilityId: " + facilityId + ", attrName: " + attrName);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributeName", attrName);

		JSONObject res = makeRpcCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params, false);
		PerunAttribute attribute = MapperUtils.mapAttribute(res);

		log.debug("getFacilityAttribute returns: {}", attribute);
		return attribute;
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws RPCException {
		log.debug("getFacilityAttributes(facilityId: {}, attrNames: {})", facilityId, attrNames);
		if (facilityId == null || attrNames == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrNames" + attrNames);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attrNames", attrNames);

		JSONArray res = makeRpcCallForArray(ATTRIBUTES_MANAGER, "getAttributes", params, false);
		Map<String, PerunAttribute> attributeMap = MapperUtils.mapAttributes(res);

		log.debug("getFacilityAttributes returns: {}", attributeMap);
		return attributeMap;
	}

	@Override
	public boolean setFacilityAttribute(Long facilityId, JSONObject attrJson) throws RPCException {
		log.debug("setFacilityAttribute(facilityId: {}, attrJson: {})", facilityId, attrJson);
		if (facilityId == null || attrJson == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrJson" + attrJson);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attribute", attrJson);

		makeRpcCallForArray(ATTRIBUTES_MANAGER, "setAttribute", params, true);

		log.debug("setFacilityAttribute returns: {}", true);
		return true;
	}

	@Override
	public boolean setFacilityAttributes(Long facilityId, JSONArray attrsJsons) throws RPCException {
		log.debug("setFacilityAttributes(facilityId: {}, attrsJsons: {})", facilityId, attrsJsons);
		if (facilityId == null || attrsJsons == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", attrJsons" + attrsJsons);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("attributes", attrsJsons);

		makeRpcCallForArray(ATTRIBUTES_MANAGER, "setAttributes", params, true);

		log.debug("setFacilityAttributes returns: {}", true);
		return true;
	}

	@Override
	public User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws RPCException {
		log.debug("getUserWithEmail({})", extLogin);
		if (extLogin == null || extLogin.isEmpty()) {
			throw new IllegalArgumentException("userId is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();

		params.put("extSourceName", extSourceName);
		params.put("extLogin", extLogin);

		JSONObject res = makeRpcCallForObject(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", params, false);
		if (res == null) {
			throw new RPCException("Should not found more than one user");
		}
		User user = MapperUtils.mapUser(res, false);
		params.clear();
		params.put("user", user.getId().intValue());
		params.put("attributeName", userEmailAttr);

		JSONObject attr = makeRpcCallForObject(ATTRIBUTES_MANAGER, "getAttribute", params, false);
		PerunAttribute attribute = MapperUtils.mapAttribute(attr);

		user.setEmail(attribute.valueAsString(false));

		log.debug("getUserWithEmail returns: {}", user);
		return user;
	}

	@Override
	public boolean addFacilityAdmin(Long facilityId, Long userId) throws RPCException {
		log.debug("addFacilityAdmin(facilityId: {}, userId:{})", facilityId, userId);
		if (facilityId == null || userId == null) {
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		}
		boolean result = addRemoveFacilityAdmin(facilityId, userId, true);

		log.debug("addFacilityAdmin returns: {}", result);
		return result;
	}

	@Override
	public Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws RPCException {
		log.debug("getFacilityIdsWhereUserIsAdmin({})", userId);
		if (userId == null) {
			throw new IllegalArgumentException("userId is null");
		}
		List<Facility> facilities = getFacilitiesWhereUserIsAdmin(userId);
		Set<Long> ids = new HashSet<>();
		facilities.forEach(f -> ids.add(f.getId()));

		log.debug("getFacilityIdsWhereUserIsAdmin returns: {}", ids);
		return ids;
	}

	@Override
	public PerunAttributeDefinition getAttributeDefinition(String attributeName) throws RPCException {
		log.debug("getAttributeDefinition({})", attributeName);
		if (attributeName == null) {
			throw new IllegalArgumentException("attributeName is null");
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", attributeName);

		JSONObject res = makeRpcCallForObject(ATTRIBUTES_MANAGER, "getAttributeDefinition", params, false);
		PerunAttributeDefinition definition = PerunAttributeDefinition.fromPerunJson(res);

		log.debug("getAttributeDefinition returns: {}", definition);
		return definition;
	}

	private JSONObject makeRpcCallForObject(String manager, String method, Map<String, Object> map, boolean post) throws RPCException {
		log.debug("makeRpcCallForObject(manager: {}, method: {}, map: {}", manager, method, map);
		String response = makeRpcCall(manager, method, map, post);
		if (response == null || response.equalsIgnoreCase("null")) {
			return null;
		}
		JSONObject result = new JSONObject(response);

		log.debug("makeRpcCallForObject returns: {}",result);
		return result;
	}

	private JSONArray makeRpcCallForArray(String manager, String method, Map<String, Object> map, boolean post) throws RPCException {
		log.debug("makeRpcCallForArray(manager: {}, method: {}, map: {}", manager, method, map);
		String response = makeRpcCall(manager, method, map, post);
		if (response == null || response.equalsIgnoreCase("null")) {
			return null;
		}
		JSONArray result = new JSONArray(response);

		log.debug("makeRpcCallForArray returns: {}",result);
		return result;
	}

	private String makeRpcCall(String manager, String method, Map<String, Object> map, boolean post) throws RPCException {
		//prepare basic auth
		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors =
				Collections.singletonList(new BasicAuthorizationInterceptor(perunRpcUser, perunRpcPassword));
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
		String actionUrl = perunRpcUrl + "/json/" + manager + '/' + method;

		//make the call
		try {
			JsonNode response;
			if (post) {
				HttpEntity<String> entity = prepareJsonBody(map);
				response = restTemplate.postForObject(actionUrl, entity, JsonNode.class);
			} else {
				response = restTemplate.postForObject(actionUrl, map, JsonNode.class);
			}

			return (response != null) ? prettyPrintJsonString(response) : null;
		} catch (HttpClientErrorException ex) {
			MediaType contentType = null;
			if (ex.getResponseHeaders() != null) {
				contentType = ex.getResponseHeaders().getContentType();
			}
			String body = ex.getResponseBodyAsString();
			if (contentType != null && "json".equals(contentType.getSubtype())) {
				try {
					new ObjectMapper().readValue(body, JsonNode.class).path("message").asText();
				} catch (IOException e) {
					log.error("cannot parse error message from JSON", e);
				}
			} else {
				log.error(ex.getMessage());
			}
			throw new RPCException("cannot connect to Perun RPC: {}" + ex.getMessage() , ex);
		} catch (IOException e) {
			log.error("cannot parse response to String", e);
			throw new RPCException("cannot connect to Perun RPC", e);
		}
	}

	private HttpEntity<String> prepareJsonBody(Map<String, Object> map) {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(obj.toString(), headers);
	}

	private boolean addRemoveFacilityAdmin(Long facilityId, Long userId, boolean add) throws RPCException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("user", userId);

		if (add) {
			makeRpcCallForObject(FACILITIES_MANAGER, "addAdmin", params, true);
		} else {
			makeRpcCallForObject(FACILITIES_MANAGER, "removeAdmin", params, true);
		}
		return true;
	}

	private static String prettyPrintJsonString(JsonNode jsonNode) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object json = mapper.readValue(jsonNode.toString(), Object.class);

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}
}
