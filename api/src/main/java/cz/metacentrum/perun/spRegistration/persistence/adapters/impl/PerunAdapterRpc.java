package cz.metacentrum.perun.spRegistration.persistence.adapters.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnectorRpc;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component("perunAdapter")
@Slf4j
public class PerunAdapterRpc implements PerunAdapter {

	private static final String USERS_MANAGER = "usersManager";
	private static final String FACILITIES_MANAGER = "facilitiesManager";
	private static final String ATTRIBUTES_MANAGER = "attributesManager";
	private static final String SEARCHER = "searcher";
	private static final String GROUPS_MANAGER = "groupsManager";
	private static final String MEMBERS_MANAGER = "membersManager";
	
	public static final String PARAM_FACILITY = "facility";
	public static final String PARAM_FORCE = "force";
	public static final String PARAM_ID = "id";
	public static final String PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES = "attributesWithSearchingValues";
	public static final String PARAM_USER = "user";
	public static final String PARAM_ATTRIBUTE_NAME = "attributeName";
	public static final String PARAM_ATTR_NAMES = "attrNames";
	public static final String PARAM_ATTRIBUTE = "attribute";
	public static final String PARAM_ATTRIBUTES = "attributes";
	public static final String PARAM_EXT_SOURCE_NAME = "extSourceName";
	public static final String PARAM_EXT_LOGIN = "extLogin";
	public static final String PARAM_ATTRIBUTE_VALUE = "attributeValue";
	public static final String PARAM_SPECIFIC_ATTRIBUTES = "specificAttributes";
	public static final String PARAM_ALL_USER_ATTRIBUTES = "allUserAttributes";
	public static final String PARAM_ONLY_DIRECT_ADMINS = "onlyDirectAdmins";

	private final PerunConnectorRpc perunConnectorRpc;
	private final ApplicationProperties applicationProperties;
	private final AttributesProperties attributesProperties;

	@Autowired
	public PerunAdapterRpc(PerunConnectorRpc perunConnectorRpc,
						   ApplicationProperties applicationProperties,
						   AttributesProperties attributesProperties)
	{
		this.perunConnectorRpc = perunConnectorRpc;
		this.applicationProperties = applicationProperties;
		this.attributesProperties = attributesProperties;
	}

	@Override
	public Facility createFacilityInPerun(JsonNode facilityJson) throws PerunUnknownException, PerunConnectionException {
		log.trace("createFacilityInPerun({})", facilityJson);

		if (Utils.checkParamsInvalid(facilityJson)) {
			log.error("Wrong parameters passed: (facilityJson: {})", facilityJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityJson);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "createFacility", params);
		Facility facility = MapperUtils.mapFacility(res);

		log.trace("createFacilityInPerun() returns: {}", facility);
		return facility;
	}

	@Override
	public Facility updateFacilityInPerun(JsonNode facilityJson) throws PerunUnknownException, PerunConnectionException {
		log.trace("updateFacilityInPerun({})", facilityJson);

		if (Utils.checkParamsInvalid(facilityJson)) {
			log.error("Wrong parameters passed: (facilityJson: {})", facilityJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityJson);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "updateFacility", params);
		Facility facility = MapperUtils.mapFacility(res);

		log.trace("updateFacilityInPerun() returns: {}", facility);
		return facility;
	}

	@Override
	public boolean deleteFacilityFromPerun(Long facilityId) throws PerunUnknownException, PerunConnectionException {
		log.trace("deleteFacilityFromPerun({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_FORCE, true);

		boolean successful = null != perunConnectorRpc.post(FACILITIES_MANAGER, "deleteFacility", params);

		log.trace("deleteFacilityFromPerun() returns: {}", successful);
		return successful;
	}

	@Override
	public Facility getFacilityById(Long facilityId) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilityById({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ID, facilityId);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "getFacilityById", params);
		Facility facility = MapperUtils.mapFacility(res);

		List<User> admins = getAdminsForFacility(facilityId, attributesProperties.getUserEmailAttrName());
		facility.setAdmins(admins);

		log.trace("getFacilityById() returns: {}", facility);
		return facility;
	}

	@Override
	public List<Facility> getFacilitiesByProxyIdentifier(String proxyIdentifierAttr, String proxyIdentifier)
			throws PerunUnknownException, PerunConnectionException {
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
		params.put(PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES, attributesWithSearchingValues);

		JsonNode res = perunConnectorRpc.post(SEARCHER, "getFacilities", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByProxyIdentifier() returns: {}", facilities);
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilitiesWhereUserIsAdmin({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_USER, userId);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "getFacilitiesWhereUserIsAdmin", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesWhereUserIsAdmin() returns: {}", facilities);
		return facilities;
	}

	@Override
	public PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilityAttribute(facilityId: {}, attrName: {})", facilityId, attrName);

		if (Utils.checkParamsInvalid(facilityId, attrName)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrName: {})", facilityId, attrName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTE_NAME, attrName);

		JsonNode res = perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", params);
		PerunAttribute attribute = MapperUtils.mapAttribute(res);

		log.trace("getFacilityAttribute() returns: {}", attribute);
		return attribute;
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilityAttributes(facilityId: {}, attrNames: {})", facilityId, attrNames);

		if (Utils.checkParamsInvalid(facilityId, attrNames)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrNames: {})", facilityId, attrNames);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTR_NAMES, attrNames);

		JsonNode res = perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params);
		Map<String, PerunAttribute> attributeMap = MapperUtils.mapAttributes(res);

		log.trace("getFacilityAttributes() returns: {}", attributeMap);
		return attributeMap;
	}

	@Override
	public boolean setFacilityAttribute(Long facilityId, JsonNode attrJson) throws PerunUnknownException, PerunConnectionException {
		log.trace("setFacilityAttribute(facilityId: {}, attrJson: {})", facilityId, attrJson);

		if (Utils.checkParamsInvalid(facilityId, attrJson)) {
			log.error("Wrong parameters passed: (facilityId: {}, attrJson: {})", facilityId, attrJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTE, attrJson);

		boolean successful = (null == perunConnectorRpc.post(ATTRIBUTES_MANAGER, "setAttribute", params));

		log.trace("setFacilityAttribute() returns: {}", successful);
		return successful;
	}

	@Override
	public boolean setFacilityAttributes(Long facilityId, JsonNode attrsJsons) throws PerunUnknownException, PerunConnectionException {
		log.trace("setFacilityAttributes(facilityId: {}, attrsJsons: {})", facilityId, attrsJsons);

		if (Utils.checkParamsInvalid(facilityId, attrsJsons)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTES, attrsJsons);

		boolean successful = (null == perunConnectorRpc.post(ATTRIBUTES_MANAGER, "setAttributes", params));

		log.trace("setFacilityAttributes() returns: {}", successful);
		return successful;
	}

	@Override
	public User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws PerunUnknownException, PerunConnectionException {
		log.trace("getUserWithEmail({})", extLogin);

		if (Utils.checkParamsInvalid(extLogin, extSourceName, userEmailAttr)) {
			log.error("Wrong parameters passed: (extLogin: {}, extSourceName: {}, userEmailAttr: {})",
					extLogin, extSourceName, userEmailAttr);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();

		params.put(PARAM_EXT_SOURCE_NAME, extSourceName);
		params.put(PARAM_EXT_LOGIN, extLogin);

		JsonNode res = perunConnectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", params);

		User user = MapperUtils.mapUser(res);
		params.clear();
		params.put(PARAM_USER, user.getId().intValue());
		params.put(PARAM_ATTRIBUTE_NAME, userEmailAttr);

		JsonNode attr = perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", params);
		PerunAttribute attribute = MapperUtils.mapAttribute(attr);
		user.setEmail(attribute.valueAsString());

		log.trace("getUserWithEmail() returns: {}", user);
		return user;
	}

	@Override
	public boolean addFacilityAdmin(Long facilityId, Long userId) throws PerunUnknownException, PerunConnectionException {
		log.trace("addFacilityAdmin(facilityId: {}, userId:{})", facilityId, userId);

		if (Utils.checkParamsInvalid(facilityId, userId)) {
			log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_USER, userId);

		boolean res = (null == perunConnectorRpc.post(FACILITIES_MANAGER, "addAdmin", params));

		log.trace("addFacilityAdmin() returns: {}", res);
		return res;
	}

	@Override
	public Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilityIdsWhereUserIsAdmin({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		List<Facility> facilities = this.getFacilitiesWhereUserIsAdmin(userId);
		if (facilities == null) {
			return new HashSet<>();
		}

		Set<Long> ids = new HashSet<>();
		facilities.forEach(f -> ids.add(f.getId()));

		log.trace("getFacilityIdsWhereUserIsAdmin() returns: {}", ids);
		return ids;
	}

	@Override
	public PerunAttributeDefinition getAttributeDefinition(String attributeName) throws PerunUnknownException, PerunConnectionException {
		log.trace("getAttributeDefinition({})", attributeName);

		if (Utils.checkParamsInvalid(attributeName)) {
			log.error("Wrong parameters passed: (attributeName: {})", attributeName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ATTRIBUTE_NAME, attributeName);

		JsonNode res = perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributeDefinition", params);
		PerunAttributeDefinition definition = MapperUtils.mapAttrDefinition(res);

		log.trace("getAttributeDefinition() returns: {}", definition);
		return definition;
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(String attrName, String attrValue) throws PerunUnknownException, PerunConnectionException {
		log.trace("getFacilitiesByAttribute(attrName: {}, attrValue: {})", attrName, attrName);

		if (Utils.checkParamsInvalid(attrName)) {
			log.error("Wrong parameters passed: (attrName: {})", attrName);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ATTRIBUTE_NAME, attrName);
		params.put(PARAM_ATTRIBUTE_VALUE, attrValue);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
		List<Facility> facilities = MapperUtils.mapFacilities(res);

		log.trace("getFacilitiesByAttribute() returns: {}", facilities);
		return facilities;
	}

	@Override
	public User getUserById(Long id) throws PerunUnknownException, PerunConnectionException {
		log.trace("getUserById({})", id);

		if (Utils.checkParamsInvalid(id)) {
			log.error("Wrong parameters passed: (id: {})", id);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ID, id);

		JsonNode res = perunConnectorRpc.post(USERS_MANAGER, "getUserById", params);
		User user = MapperUtils.mapUser(res);

		log.trace("getUserById() returns: {}", user);
		return user;
	}

	/* PRIVATE METHODS */

	private List<User> getAdminsForFacility(Long facility, String userEmailAttr) throws PerunUnknownException, PerunConnectionException {
		log.trace("getAdminsForFacility({})", facility);
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facility);
		params.put(PARAM_SPECIFIC_ATTRIBUTES, Collections.singletonList(userEmailAttr));
		params.put(PARAM_ALL_USER_ATTRIBUTES, false);
		params.put(PARAM_ONLY_DIRECT_ADMINS, false);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "getRichAdmins", params);
		List<User> admins = MapperUtils.mapUsers(res, userEmailAttr);
		for (User u: admins) {
			u.setAdmin(applicationProperties.getAdminIds().contains(u.getId()));
		}

		log.trace("getAdminsForFacility() returns: {}", admins);
		return admins;
	}

	@Override
	public Group createGroup(Long parentGroupId, Group group) throws PerunUnknownException, PerunConnectionException {
		log.trace("createGroup({}, {})", parentGroupId, group);

		if (Utils.checkParamsInvalid(parentGroupId, group)) {
			log.error("Wrong parameters passed: (parentGroupId: {}, group: {})", parentGroupId, group);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("parentGroup", parentGroupId);
		params.put("group", group.toJson());

		JsonNode res = perunConnectorRpc.post(GROUPS_MANAGER, "createGroup", params);
		Group g = MapperUtils.mapGroup(res);

		log.trace("createGroup({}, {}) returns: {}", parentGroupId, group, g);
		return g;
	}

	@Override
	public boolean deleteGroup(Long groupId) throws PerunUnknownException, PerunConnectionException {
		log.trace("deleteGroup({})", groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("force", true);

		JsonNode res = perunConnectorRpc.post(GROUPS_MANAGER, "deleteGroup", params);
		boolean result = res == null || res.isNull();

		log.trace("deleteGroup({}) returns: {}", groupId, result);
		return result;
	}

	@Override
	public boolean addGroupAsAdmins(Long facilityId, Long groupId) throws PerunUnknownException, PerunConnectionException {
		log.trace("addGroupAsAdmins({}, {})", facilityId, groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "addAdmin", params);
		boolean result = res == null || res.isNull();

		log.trace("addGroupAsAdmins({}, {}) returns: {}", facilityId, groupId, result);
		return result;
	}

	@Override
	public boolean removeGroupFromAdmins(Long facilityId, Long groupId) throws PerunUnknownException, PerunConnectionException {
		log.trace("removeGroupFromAdmins({}, {})", facilityId, groupId);

		if (Utils.checkParamsInvalid(groupId)) {
			log.error("Wrong parameters passed: (groupId: {})", groupId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = perunConnectorRpc.post(FACILITIES_MANAGER, "removeAdmin", params);
		boolean result = res == null || res.isNull();

		log.trace("removeGroupFromAdmins({}, {}) returns: {}", facilityId, groupId, result);
		return result;
	}

	@Override
	public Long getMemberIdByUser(Long vo, Long user) throws PerunUnknownException, PerunConnectionException {
		log.trace("getMemberIdByUser({}, {})", vo, user);

		if (Utils.checkParamsInvalid(vo, user)) {
			log.error("Wrong parameters passed: (vo: {}, user: {})", vo, user);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("vo", vo);
		params.put("user", user);

		JsonNode res = perunConnectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", params);
		Long id = res.get("id").asLong();

		log.trace("getMemberIdByUser({}, {}) returns: {}", vo, user, id);
		return id;
	}

	@Override
	public boolean addMemberToGroup(Long groupId, Long memberId)
			throws PerunUnknownException, PerunConnectionException
	{
		log.trace("addMemberToGroup({}, {})", groupId, memberId);

		if (Utils.checkParamsInvalid(groupId, memberId)) {
			log.error("Wrong parameters passed: (groupId: {}, memberId: {})", groupId, memberId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("member", memberId);

		JsonNode res = perunConnectorRpc.post(GROUPS_MANAGER, "addMember", params);
		boolean result = res == null || res.isNull();

		log.trace("addMemberToGroup({}, {}) returns: {}", groupId, memberId, result);
		return result;
	}

}
