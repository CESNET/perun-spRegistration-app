package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Attribute definition from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PerunAttributeDefinition extends PerunEntity {

	public static final String KEY_ID = "id";
	public static final String KEY_FRIENDLY_NAME = "friendlyName";
	public static final String KEY_NAMESPACE = "namespace";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DISPLAY_NAME = "displayName";
	public static final String KEY_WRITABLE = "writable";
	public static final String KEY_UNIQUE = "unique";
	public static final String KEY_ENTITY = "entity";
	public static final String KEY_BASE_FRIENDLY_NAME = "baseFriendlyName";
	public static final String KEY_FRIENDLY_NAME_PARAMETER = "friendlyNameParameter";
	public static final String KEY_BEAN_NAME = "beanName";

	private String friendlyName;
	private String namespace;
	private String description;
	private String type;
	private String displayName;
	private boolean writable;
	private boolean unique;
	private String entity;
	private final String beanName = "Attribute";
	private String baseFriendlyName;
	private String friendlyNameParameter;


	public PerunAttributeDefinition(Long id, String friendlyName, String namespace, String description, String type,
									String displayName, boolean writable, boolean unique, String entity,
									String baseFriendlyName, String friendlyNameParameter) {
		super(id);
		this.friendlyName = friendlyName;
		this.namespace = namespace;
		this.description = description;
		this.type = type;
		this.displayName = displayName;
		this.writable = writable;
		this.unique = unique;
		this.entity = entity;
		this.baseFriendlyName = baseFriendlyName;
		this.friendlyNameParameter = friendlyNameParameter;
	}

	public static PerunAttributeDefinition fromPerunJson(JsonNode jsonNode) {
		if (Utils.checkParamsInvalid(jsonNode)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Long id = jsonNode.get(KEY_ID).asLong();
		String friendlyName = jsonNode.get(KEY_FRIENDLY_NAME).textValue();
		String namespace = jsonNode.get(KEY_NAMESPACE).textValue();
		String description = jsonNode.get(KEY_DESCRIPTION).textValue();
		String type = jsonNode.get(KEY_TYPE).textValue();
		String displayName = jsonNode.get(KEY_DISPLAY_NAME).textValue();
		boolean writable = jsonNode.get(KEY_WRITABLE).asBoolean();
		boolean unique = jsonNode.get(KEY_UNIQUE).asBoolean();
		String entity = jsonNode.get(KEY_ENTITY).textValue();
		String baseFriendlyName = jsonNode.get(KEY_BASE_FRIENDLY_NAME).textValue();
		String friendlyNameParameter = jsonNode.get(KEY_FRIENDLY_NAME_PARAMETER).textValue();

		return new PerunAttributeDefinition(id, friendlyName, namespace, description, type, displayName, writable,
				unique, entity, baseFriendlyName, friendlyNameParameter);
	}


	public JsonNode toJson() {
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put(KEY_ID, super.getId());
		res.put(KEY_FRIENDLY_NAME, friendlyName);
		res.put(KEY_NAMESPACE, namespace);
		res.put(KEY_DESCRIPTION, description);
		res.put(KEY_TYPE, type);
		res.put(KEY_DISPLAY_NAME, displayName);
		res.put(KEY_WRITABLE, writable);
		res.put(KEY_ENTITY, entity);
		res.put(KEY_BEAN_NAME, beanName);
		res.put(KEY_UNIQUE, unique);
		res.put(KEY_BASE_FRIENDLY_NAME, baseFriendlyName);
		res.put(KEY_FRIENDLY_NAME_PARAMETER, friendlyNameParameter);

		return res;
	}

	@JsonIgnore
	public String getFullName() {
		return this.namespace + ':' + this.friendlyName;
	}

}
