package cz.metacentrum.perun.spRegistration.persistence.adapters.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.Member;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.AdapterUtils;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnectorRpc;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	@NonNull private final PerunConnectorRpc perunRpc;
	@NonNull private final ApplicationProperties applicationProperties;
	@NonNull private final AttributesProperties attributesProperties;

	@Autowired
	public PerunAdapterRpc(PerunConnectorRpc perunConnectorRpc,
						   ApplicationProperties applicationProperties,
						   AttributesProperties attributesProperties)
	{
		this.perunRpc = perunConnectorRpc;
		this.applicationProperties = applicationProperties;
		this.attributesProperties = attributesProperties;
	}

	@Override
	public Facility createFacilityInPerun(@NonNull String name, String description)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name cannot be NULL nor EMPTY");
		}
		if (description == null) {
			description = "";
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, AdapterUtils.generateFacilityJson(name, description));

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "createFacility", params);
		return MapperUtils.mapFacility(response);
	}

	@Override
	public Facility updateFacilityInPerun(@NonNull JsonNode facilityJson)
			throws PerunUnknownException, PerunConnectionException
	{
		if (facilityJson.isNull()) {
			throw new IllegalArgumentException("FacilityJson cannot be NULL JSON object");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityJson);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "updateFacility", params);
		return MapperUtils.mapFacility(response);
	}

	@Override
	public boolean deleteFacilityFromPerun(@NonNull Long facilityId)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_FORCE, true);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "deleteFacility", params);
		return (!response.isNull());
	}

	@Override
	public Facility getFacilityById(@NonNull Long facilityId)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ID, facilityId);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "getFacilityById", params);
		Facility facility = MapperUtils.mapFacility(response);
		if (facility == null) {
			return null;
		}

		List<User> admins = this.getAdminsForFacility(facilityId, attributesProperties.getNames().getUserEmail());
		facility.setManagers(admins);
		return facility;
	}

	@Override
	public List<Facility> getFacilitiesByProxyIdentifier(@NonNull String proxyIdentifierAttr,
														 @NonNull String proxyIdentifier)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(proxyIdentifier)) {
			throw new IllegalArgumentException("ProxyIdentifierAttr cannot be empty");
		} else if (!StringUtils.hasText(proxyIdentifier)) {
			throw new IllegalArgumentException("ProxyIdentifier cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES,
				Collections.singletonMap(proxyIdentifierAttr, proxyIdentifier)
		);

		JsonNode response = perunRpc.call(SEARCHER, "getFacilities", params);
		return MapperUtils.mapFacilities(response);
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(@NonNull Long userId)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_USER, userId);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "getFacilitiesWhereUserIsAdmin", params);
		return MapperUtils.mapFacilities(response);
	}

	@Override
	public PerunAttribute getFacilityAttribute(@NonNull Long facilityId, @NonNull String attrName)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(attrName)) {
			throw new IllegalArgumentException("AttrName cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTE_NAME, attrName);

		JsonNode response = perunRpc.call(ATTRIBUTES_MANAGER, "getAttribute", params);
		return MapperUtils.mapPerunAttribute(response);
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(@NonNull Long facilityId, @NonNull List<String> attrNames)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTR_NAMES, attrNames);

		JsonNode response = perunRpc.call(ATTRIBUTES_MANAGER, "getAttributes", params);
		return MapperUtils.mapAttributes(response);
	}

	@Override
	public boolean setFacilityAttribute(@NonNull Long facilityId, @NonNull  JsonNode attrJson)
			throws PerunUnknownException, PerunConnectionException
	{
		if (attrJson.isNull()) {
			throw new IllegalArgumentException("AttrJson cannot be null JSON object");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTE, attrJson);

		JsonNode response = perunRpc.call(ATTRIBUTES_MANAGER, "setAttribute", params);
		return response.isNull();
	}

	@Override
	public boolean setFacilityAttributes(@NonNull Long facilityId, @NonNull JsonNode attrJsons)
			throws PerunUnknownException, PerunConnectionException
	{
		if (attrJsons.isNull()) {
			throw new IllegalArgumentException("AttrJsons cannot be null JSON object");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facilityId);
		params.put(PARAM_ATTRIBUTES, attrJsons);

		JsonNode response = perunRpc.call(ATTRIBUTES_MANAGER, "setAttributes", params);
		return response.isNull();
	}

	@Override
	public User getUserWithEmail(@NonNull String extLogin, @NonNull String extSourceName, @NonNull String userEmailAttr)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(userEmailAttr)) {
			throw new IllegalArgumentException("UserEmailAttr cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_EXT_SOURCE_NAME, extSourceName);
		params.put(PARAM_EXT_LOGIN, extLogin);

		JsonNode response = perunRpc.call(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", params);
		User user = MapperUtils.mapUser(response);
		if (user == null) {
			return null;
		} else if (user.getId() == null) {
			return user;
		}

		PerunAttribute emailAttribute = this.getUserAttribute(user.getId(), userEmailAttr);
		if (emailAttribute != null) {
			user.setEmail(emailAttribute.valueAsString());
		}
		return user;
	}

	@Override
	public Set<Long> getFacilityIdsWhereUserIsAdmin(@NonNull Long userId)
			throws PerunUnknownException, PerunConnectionException
	{
		List<Facility> facilities = this.getFacilitiesWhereUserIsAdmin(userId);
		if (facilities == null) {
			return new HashSet<>();
		}
		return facilities.stream().map(Facility::getId).collect(Collectors.toSet());
	}

	@Override
	public PerunAttributeDefinition getAttributeDefinition(@NonNull String attributeName)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(attributeName)) {
			throw new IllegalArgumentException("AttributeName cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ATTRIBUTE_NAME, attributeName);

		JsonNode response = perunRpc.call(ATTRIBUTES_MANAGER, "getAttributeDefinition", params);
		return MapperUtils.mapAttributeDefinition(response);
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(@NonNull String attrName, @NonNull String attrValue)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(attrName)) {
			throw new IllegalArgumentException("AttrName cannot be empty");
		} else if (!StringUtils.hasText(attrValue)) {
			throw new IllegalArgumentException("AttrValue cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ATTRIBUTE_NAME, attrName);
		params.put(PARAM_ATTRIBUTE_VALUE, attrValue);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
		return MapperUtils.mapFacilities(response);
	}

	@Override
	public User getUserById(@NonNull Long id) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_ID, id);

		JsonNode response = perunRpc.call(USERS_MANAGER, "getUserById", params);
		return MapperUtils.mapUser(response);
	}

	private List<User> getAdminsForFacility(@NonNull Long facility, @NonNull String userEmailAttr)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(userEmailAttr)) {
			throw new IllegalArgumentException("UserEmailAttr cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_FACILITY, facility);
		params.put(PARAM_SPECIFIC_ATTRIBUTES, Collections.singletonList(userEmailAttr));
		params.put(PARAM_ALL_USER_ATTRIBUTES, false);
		params.put(PARAM_ONLY_DIRECT_ADMINS, false);

		JsonNode response = perunRpc.call(FACILITIES_MANAGER, "getRichAdmins", params);
		List<User> admins = MapperUtils.mapUsers(response, userEmailAttr);
		for (User u: admins) {
			u.setAppAdmin(applicationProperties.getAdminIds().contains(u.getId()));
		}

		return admins;
	}

	@Override
	public Group createGroup(@NonNull Long parentGroupId, @NonNull Group group)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("parentGroup", parentGroupId);
		params.put("group", group.toJson());

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "createGroup", params);
		return MapperUtils.mapGroup(res);
	}

	@Override
	public boolean deleteGroup(@NonNull Long groupId) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("force", true);

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "deleteGroup", params);
		return res == null || res.isNull();
	}

	@Override
	public boolean addGroupAsAdmins(@NonNull Long facilityId, @NonNull Long groupId)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = perunRpc.call(FACILITIES_MANAGER, "addAdmin", params);
		return res == null || res.isNull();
	}

	@Override
	public boolean removeGroupFromAdmins(@NonNull Long facilityId, @NonNull Long groupId)
			throws PerunUnknownException, PerunConnectionException
	{
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("facility", facilityId);
		params.put("authorizedGroup", groupId);

		JsonNode res = perunRpc.call(FACILITIES_MANAGER, "removeAdmin", params);
		return res == null || res.isNull();
	}

	@Override
	public Long getMemberIdByUser(Long vo, Long user) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("vo", vo);
		params.put("user", user);

		JsonNode res = perunRpc.call(MEMBERS_MANAGER, "getMemberByUser", params);
		if (res == null || (res instanceof NullNode) || res.isNull()) {
			throw new PerunUnknownException("User is not member in VO");
		}
		return res.get("id").asLong();
	}

	@Override
	public boolean addMemberToGroup(Long groupId, Long memberId) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("member", memberId);

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "addMember", params);
		return res == null || res.isNull();
	}

	@Override
	public boolean removeMemberFromGroup(Long groupId, Long memberId) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("member", memberId);

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "removeMember", params);
		return res == null || res.isNull();
	}

	@Override
	public boolean removeMembersFromGroup(Long groupId, List<Long> membersIds) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);
		params.put("members", membersIds);

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "removeMembers", params);
		return res == null || res.isNull();
	}

	private PerunAttribute getUserAttribute(Long userId, String attributeName)
			throws PerunUnknownException, PerunConnectionException
	{
		if (!StringUtils.hasText(attributeName)) {
			throw new IllegalArgumentException("AttributeName cannot be empty");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put(PARAM_USER, userId);
		params.put(PARAM_ATTRIBUTE_NAME, attributeName);

		JsonNode attr = perunRpc.call(ATTRIBUTES_MANAGER, "getAttribute", params);
		return MapperUtils.mapPerunAttribute(attr);
	}

	@Override
	public List<Member> getGroupMembers(Long groupId) throws PerunUnknownException, PerunConnectionException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("group", groupId);

		JsonNode res = perunRpc.call(GROUPS_MANAGER, "getGroupMembers", params);

		return MapperUtils.mapMembers(res);
	}

}
