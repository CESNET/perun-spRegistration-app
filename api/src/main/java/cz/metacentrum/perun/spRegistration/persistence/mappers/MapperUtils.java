package cz.metacentrum.perun.spRegistration.persistence.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapperUtils class for mapping objects from RPC responses.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class MapperUtils {

	private static final Logger log = LoggerFactory.getLogger(MapperUtils.class);

	/**
	 * Map JSON response from Perun RPC to Facility object.
	 * @param facilityJson JSON from Perun with facility.
	 * @return Mapped Facility object.
	 * @throws IllegalArgumentException Thrown when param "facilityJson" is NULL, equal to JSONObject.NULL or empty.
	 */
	public static Facility mapFacility(JsonNode facilityJson) {
		log.trace("mapFacility({})", facilityJson);

		if (Utils.checkParamsInvalid(facilityJson)) {
			log.error("Wrong parameters passed: (facilityJson: {})", facilityJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		} else {
			Facility facility = Facility.fromPerunJson(facilityJson);

			log.trace("mapFacility() returns: {}", facility);
			return facility;
		}
	}

	/**
	 * Map JSON response from Perun RPC to List of Facilities.
	 * @param facilitiesJson JSON from Perun with facilities.
	 * @return Mapped List of Facility objects (filled or empty).
	 * @throws IllegalArgumentException Thrown when param "facilitiesJson" is NULL, equal to JSONObject.NULL or empty.
	 */
	public static List<Facility> mapFacilities(JsonNode facilitiesJson) {
		log.trace("mapFacilities({})", facilitiesJson);

		List<Facility> facilityList = new ArrayList<>();

		if (Utils.checkParamsInvalid(facilitiesJson)) {
			log.error("Wrong parameters passed: (facilitiesJson: {})", facilitiesJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		for (int i = 0; i < facilitiesJson.size(); i++) {
			JsonNode facilityJson = facilitiesJson.get(i);
			Facility facility = Facility.fromPerunJson(facilityJson);
			facilityList.add(facility);
		}

		log.trace("mapFacilities() returns: {}", facilityList);
		return facilityList;
	}

	/**
	 * Map JSON response from Perun RPC to User object.
	 * @param json JSON from Perun with user.
	 * @return Mapped User object.
	 * @throws IllegalArgumentException Thrown when param "json" is NULL, equal to JSONObject.NULL or empty.
	 */
	public static User mapUser(JsonNode json) {
		return mapUser(json, null);
	}

	/**
	 * Map JSON response from Perun RPC to User object.
	 * @param json JSON from Perun with user.
	 * @param userMailAttr mapping of user email attribute, pass NULL if JSON is not RichUser
	 * @return Mapped User object.
	 * @throws IllegalArgumentException Thrown when param "json" is NULL, equal to JSONObject.NULL or empty.
	 */
	public static User mapUser(JsonNode json, String userMailAttr) {
		log.trace("mapUser(json: {}, userMailAttr: {})", json, userMailAttr);
		User user;

		if (Utils.checkParamsInvalid(json)) {
			log.error("Wrong parameters passed: (json: {})", json);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		user = User.fromPerunJson(json);
		if (userMailAttr != null) {
			ArrayNode attrs = JsonNodeFactory.instance.arrayNode();

			if (json.hasNonNull("attributes")) {
				attrs = (ArrayNode) json.get("attributes");
			} else if (json.hasNonNull("userAttributes")) {
				attrs = (ArrayNode) json.get("userAttributes");
			}

			for (int i = 0; i < attrs.size(); i++) {
				JsonNode attrJson = attrs.get(i);
				String namespace = attrJson.get("namespace").textValue();
				String friendlyName = attrJson.get("friendlyName").textValue();
				String fullAttrName = namespace + ':' + friendlyName;
				if (userMailAttr.equals(fullAttrName)) {
					user.setEmail(attrJson.get("value").textValue());
				}
			}
		}

		log.trace("mapUser() returns: {}", user);
		return user;
	}

	/**
	 * Map JSON from Perun RPC to Map of Attributes, where key = attribute name, value = attribute.
	 * @param attrsJson JSON from Perun with attributes.
	 * @return Map of Attributes (filled or empty).
	 * @throws IllegalArgumentException Thrown when input is NULL, equal to JSONObject.NULL or empty.
	 */
	public static Map<String, PerunAttribute> mapAttributes(JsonNode attrsJson) {
		log.trace("mapAttributes({})", attrsJson);

		if (Utils.checkParamsInvalid()) {
			log.error("Wrong parameters passed: (attrsJson: {})", attrsJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Map<String, PerunAttribute> attributesMap = new HashMap<>();
		for (int i = 0; i < attrsJson.size(); i++) {
			JsonNode attrJson = attrsJson.get(i);
			PerunAttribute a = PerunAttribute.fromJsonOfPerun(attrJson);
			PerunAttributeDefinition def = a.getDefinition();
			a.setDefinition(def);
			a.setFullName(def.getFullName());
			attributesMap.put(def.getFullName(), a);
		}

		log.trace("mapAttributes() returns: {}", attributesMap);
		return attributesMap;
	}

	/**
	 * Map JSON from Perun RPC to PerunAttribute.
	 * @param attrJson JSON from Perun with attribute.
	 * @return Mapped PerunAttribute object.
	 * @throws IllegalArgumentException Thrown when input is NULL, equal to JSONObject.NULL or empty.
	 */
	public static PerunAttribute mapAttribute(JsonNode attrJson) {
		log.trace("mapAttribute({})", attrJson);

		if (Utils.checkParamsInvalid(attrJson)) {
			log.error("Wrong parameters passed: (attrJson: {})", attrJson);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		PerunAttribute perunAttribute = PerunAttribute.fromJsonOfPerun(attrJson);

		log.trace("mapAttribute() returns: {}", perunAttribute);
		return perunAttribute;
	}

	/**
	 * Map JSON from Perun RPC to PerunAttributeDefinition
	 * @param json JSON from Perun with attribute definition.
	 * @return Mapped PerunAttributeDefinition object.
	 */
	public static PerunAttributeDefinition mapAttrDefinition(JsonNode json) {
		log.trace("mapAttrDefinition({})", json);
		PerunAttributeDefinition perunAttributeDefinition;

		if (Utils.checkParamsInvalid(json)) {
			log.error("Wrong parameters passed: (json: {})", json);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		perunAttributeDefinition = PerunAttributeDefinition.fromPerunJson(json);

		log.trace("mapAttrDefinition() returns: {}", perunAttributeDefinition);
		return perunAttributeDefinition;
	}

	public static List<User> mapUsers(JsonNode jsonArray, String userMailAttr) {
		List<User> mappedUsers = new ArrayList<>();

		if (Utils.checkParamsInvalid(jsonArray)) {
			log.error("Wrong parameters passed: (json: {})", jsonArray);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		for (int i = 0; i < jsonArray.size(); i++) {
			mappedUsers.add(mapUser(jsonArray.get(i), userMailAttr));
		}

		return mappedUsers;
	}

	public static Group mapGroup(JsonNode res) {
		if (Utils.checkParamsInvalid(res)) {
			log.error("Wrong parameters passed: (res: {})", res);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Long id = res.get("id").asLong();
		String shortName = res.get("shortName").asText();
		String name = res.get("name").asText();
		String description = res.get("description").asText();
		Long parentGroupId = res.get("parentGroupId").asLong();
		Long voId = res.get("voId").asLong();

		return new Group(id, name, shortName, description, parentGroupId, voId);
	}
}
