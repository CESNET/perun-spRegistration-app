package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

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

	@NonNull private String perunName;
	 private String perunDescription = "";
	private final Map<String, String> name = new HashMap<>();
	private final Map<String, String> description = new HashMap<>();;
	private ServiceEnvironment environment;
	private ServiceProtocol protocol;
	private final List<User> managers = new ArrayList<>();
	private Long activeRequestId;
	private final Map<AttributeCategory, Map<String, PerunAttribute>> attributes = new HashMap<>();

	public Facility(Long id) {
		super(id);
	}

	public Facility(Long id, @NonNull String perunName, String perunDescription) {
		super(id);
		this.setPerunName(perunName);
		this.setPerunDescription(perunDescription);
	}

	public void setPerunName(String perunName) {
		if (!StringUtils.hasText(perunName)) {
			this.perunName = "";
		}

		this.perunName = perunName;
	}

	public void setName(Map<String, String> name) {
		this.name.clear();
		if (name != null) {
			this.name.putAll(name);
		}
	}

	public void setDescription(Map<String, String> description) {
		this.description.clear();
		if (description != null) {
			this.description.putAll(description);
		}
	}

	public void setManagers(List<User> managers) {
		this.managers.clear();
		if (managers != null) {
			this.managers.addAll(managers);
		}
	}

	public void setAttributes(Map<AttributeCategory, Map<String, PerunAttribute>> attributes) {
		this.attributes.clear();
		if (attributes != null) {
			this.attributes.putAll(attributes);
		}
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

}
