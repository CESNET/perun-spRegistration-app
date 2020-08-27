package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of Perun Facility.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Facility extends PerunEntity {

	private String perunName;
	private String perunDescription;
	private Map<String, String> name = new HashMap<>();
	private Map<String, String> description = new HashMap<>();;
	private boolean oidc;
	private boolean saml;
	private Long activeRequestId;
	private boolean testEnv;
	private boolean editable = false;
	private List<User> admins = new ArrayList<>();
	private Map<AttributeCategory, Map<String, PerunAttribute>> attributes = new HashMap<>();

	public Facility(Long id) {
		super(id);
	}

	public Facility(Long id, String perunName, String perunDescription) {
		super(id);
		this.perunName = perunName;
		this.perunDescription = perunDescription;
	}

	/**
	 * Convert object to JSON representation
	 * @return JSON String
	 */
	public JsonNode toJson() {
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		if (this.getId() == null) {
			res.set("id", JsonNodeFactory.instance.nullNode());
		} else {
			res.put("id", getId());
		}
		res.put("name", perunName);
		res.put("description", perunDescription);
		res.put("beanName", "facility");

		return res;
	}

	/**
	 * Convert JSON from perun to facility object
	 * @param json JSON from Perun
	 * @return Facility object or null
	 */
	public static Facility fromPerunJson(JsonNode json) {
		if (Utils.checkParamsInvalid(json)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Long id = json.get("id").asLong();
		String name = json.get("name").textValue();
		String description = json.hasNonNull("description") ? json.get("description").textValue() : null;

		return new Facility(id, name, description);
	}

}
