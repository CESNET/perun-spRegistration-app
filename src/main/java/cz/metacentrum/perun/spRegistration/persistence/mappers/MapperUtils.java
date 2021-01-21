package cz.metacentrum.perun.spRegistration.persistence.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.Member;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * MapperUtils class for mapping objects from RPC responses.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class MapperUtils {
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String MIDDLE_NAME = "middleName";
	public static final String TITLE_BEFORE = "titleBefore";
	public static final String TITLE_AFTER = "titleAfter";
	public static final String ATTRIBUTES = "attributes";
	public static final String USER_ATTRIBUTES = "userAttributes";
	public static final String VALUE = "value";
	public static final String NAMESPACE = "namespace";
	public static final String FRIENDLY_NAME = "friendlyName";
	public static final String TYPE = "type";
	public static final String DISPLAY_NAME = "displayName";
	public static final String WRITABLE = "writable";
	public static final String UNIQUE = "unique";
	public static final String ENTITY = "entity";
	public static final String BASE_FRIENDLY_NAME = "baseFriendlyName";
	public static final String FRIENDLY_NAME_PARAMETER = "friendlyNameParameter";
	public static final String BEAN_NAME = "beanName";
	public static final String SHORT_NAME = "shortName";
	public static final String PARENT_GROUP_ID = "parentGroupId";
	public static final String VO_ID = "voId";
	public static final String USER_ID = "userId";
	public static final String STATUS = "status";

	public static Facility mapFacility(@NonNull JsonNode json) {
		String[] requiredParams = new String[] {ID, NAME};
		if (!MapperUtils.hasRequiredFields(json, requiredParams)) {
			log.warn("Facility JSON {} does not have all required fields {}", json, requiredParams);
			return null;
		}

		Long id = json.get(ID).asLong();
		String name = json.get(NAME).textValue();
		String description = json.hasNonNull(DESCRIPTION) ? json.get(DESCRIPTION).textValue() : null;

		return new Facility(id, name, description);
	}

	public static List<Facility> mapFacilities(@NonNull JsonNode json) {
		List<Facility> facilities = new ArrayList<>();
		if (!json.isNull() && json.isArray()) {
			for (JsonNode subJson: json) {
				Facility mapped = MapperUtils.mapFacility(subJson);
				if (mapped != null) {
					facilities.add(mapped);
				}
			}
		}

		return facilities;
	}

	public static User mapUser(@NonNull JsonNode json) {
		return MapperUtils.mapUser(json, null);
	}

	public static User mapUser(@NonNull JsonNode json, String userMailAttr) {
		String[] requiredParams = new String[] {ID, LAST_NAME};
		if (!MapperUtils.hasRequiredFields(json, requiredParams)) {
			return null;
		}
		Long id = json.get(ID).asLong();
		String firstName = json.hasNonNull(FIRST_NAME) ? json.get(FIRST_NAME).textValue() : null;
		String middleName = json.hasNonNull(MIDDLE_NAME) ? json.get(MIDDLE_NAME).textValue() : null;
		String lastName = json.get(LAST_NAME).textValue();
		String titleBefore = json.hasNonNull(TITLE_BEFORE) ? json.get(TITLE_BEFORE).textValue() : null;
		String titleAfter = json.hasNonNull(TITLE_AFTER) ? json.get(TITLE_AFTER).textValue() : null;

		String name = MapperUtils.composeNameForUser(firstName, middleName, lastName, titleBefore, titleAfter);

		User user = new User(id);
		user.setName(name);

		if (userMailAttr != null) {
			user.setEmail(MapperUtils.extractUserEmail(json, userMailAttr));
		}
		return user;
	}

	public static List<User> mapUsers(@NonNull JsonNode json, String userMailAttr) {
		List<User> mappedUsers = new ArrayList<>();

		if (!json.isNull() && json.isArray()) {
			for (JsonNode subJson: json) {
				User mapped = MapperUtils.mapUser(subJson, userMailAttr);
				if (mapped != null) {
					mappedUsers.add(mapped);
				}
			}
		}

		return mappedUsers;
	}

	public static PerunAttributeDefinition mapAttributeDefinition(@NonNull JsonNode json) {
		String[] requiredParams = new String[] {ID, FRIENDLY_NAME, NAMESPACE, DESCRIPTION, TYPE,
				DISPLAY_NAME, WRITABLE, UNIQUE, ENTITY, BASE_FRIENDLY_NAME, FRIENDLY_NAME_PARAMETER};
		if (!MapperUtils.hasRequiredFields(json, requiredParams)) {
			return null;
		}

		Long id = json.get(ID).asLong();
		String friendlyName = json.get(FRIENDLY_NAME).textValue();
		String namespace = json.get(NAMESPACE).textValue();
		String description = json.get(DESCRIPTION).textValue();
		String type = json.get(TYPE).textValue();
		String displayName = json.get(DISPLAY_NAME).textValue();
		boolean writable = json.get(WRITABLE).asBoolean();
		boolean unique = json.get(UNIQUE).asBoolean();
		String entity = json.get(ENTITY).textValue();
		String baseFriendlyName = json.get(BASE_FRIENDLY_NAME).textValue();
		String friendlyNameParameter = json.get(FRIENDLY_NAME_PARAMETER).textValue();

		return new PerunAttributeDefinition(id, friendlyName, namespace, description, type, displayName, writable,
				unique, entity, baseFriendlyName, friendlyNameParameter);
	}

	public static Map<String, PerunAttribute> mapAttributes(@NonNull JsonNode json) {
		Map<String, PerunAttribute> attributeMap = new HashMap<>();
		if (!json.isNull() && json.isArray()) {
			for (JsonNode subJson: json) {
				PerunAttribute a = MapperUtils.mapPerunAttribute(subJson);
				if (a == null) {
					continue;
				}

				PerunAttributeDefinition def = a.getDefinition();
				a.setFullName(def.getFullName());
				attributeMap.put(def.getFullName(), a);
			}
		}

		return attributeMap;
	}

	public static PerunAttribute mapPerunAttribute(JsonNode json) {
		PerunAttributeDefinition definition = MapperUtils.mapAttributeDefinition(json);
		if (definition == null) {
			return null;
		}

		JsonNode value = json.get(VALUE);
		PerunAttribute attr = new PerunAttribute();
		attr.setDefinition(definition);
		attr.setValue(value);

		return attr;
	}

	public static Group mapGroup(@NonNull JsonNode json) {
		String[] requiredParams = new String[] {ID, SHORT_NAME, NAME, VO_ID};
		if (!MapperUtils.hasRequiredFields(json, requiredParams)) {
			return null;
		}

		Long id = json.get(ID).asLong();
		String shortName = json.get(SHORT_NAME).asText();
		String name = json.get(NAME).asText();
		String description = json.get(DESCRIPTION).asText();
		Long parentGroupId = null;
		if (json.hasNonNull(PARENT_GROUP_ID)) {
			json.get(PARENT_GROUP_ID).asLong();
		}
		Long voId = json.get(VO_ID).asLong();

		return new Group(id, name, shortName, description, parentGroupId, voId);
	}

	public static Member mapMember(@NonNull JsonNode json) {
		String[] requiredParams = new String[] {ID, USER_ID, VO_ID, STATUS};
		if (!MapperUtils.hasRequiredFields(json, requiredParams)) {
			return null;
		}

		Long id = json.get(ID).asLong();
		Long userId = json.get(USER_ID).asLong();
		Long voId = json.get(VO_ID).asLong();
		String status = json.get(STATUS).asText();

		return new Member(id, userId, voId, status);
	}

	public static List<Member> mapMembers(@NonNull JsonNode json) {
		List<Member> members = new ArrayList<>();

		if (!json.isNull() && json.isArray()) {
			for (JsonNode subJson: json) {
				Member mappedMember = MapperUtils.mapMember(subJson);
				members.add(mappedMember);
			}
		}

		return members;
	}

	// private methods

	private static boolean hasRequiredFields(@NonNull JsonNode json, @NonNull String[] params) {
		if (json.isNull()) {
			return false;
		}

		for (String param: params) {
			if (! json.hasNonNull(param)) {
				return false;
			}
		}
		
		return true;
	}

	private static String composeNameForUser(String firstName, String middleName,
											 String lastName, String titleBefore, String titleAfter)
	{
		StringJoiner joiner = new StringJoiner(" ");
		if (StringUtils.hasText(titleBefore)) {
			joiner.add(titleBefore);
		}

		if (StringUtils.hasText(firstName)) {
			joiner.add(firstName);
		}

		if (StringUtils.hasText(middleName)) {
			joiner.add(middleName);
		}

		if (StringUtils.hasText(lastName)) {
			joiner.add(lastName);
		}

		if (StringUtils.hasText(titleAfter)) {
			joiner.add(titleAfter);
		}

		return joiner.toString();
	}

	private static String extractUserEmail(@NonNull JsonNode json, @NonNull String userEmailAttr) {
		ArrayNode attrs = JsonNodeFactory.instance.arrayNode();

		if (json.hasNonNull(ATTRIBUTES)) {
			attrs = (ArrayNode) json.get(ATTRIBUTES);
		} else if (json.hasNonNull(USER_ATTRIBUTES)) {
			attrs = (ArrayNode) json.get(USER_ATTRIBUTES);
		}

		for (JsonNode attrJson: attrs) {
			String namespace = attrJson.get(NAMESPACE).textValue();
			String friendlyName = attrJson.get(FRIENDLY_NAME).textValue();
			String fullAttrName = namespace + ':' + friendlyName;
			if (userEmailAttr.equals(fullAttrName)) {
				return attrJson.get(VALUE).textValue();
			}
		}

		return null;
	}

}
