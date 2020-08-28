package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

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

	@NonNull private String friendlyName;
	@NonNull private String namespace;
	@NonNull private String description;
	@NonNull private String type;
	@NonNull private String displayName;
	private boolean writable;
	private boolean unique;
	@NonNull private String entity;
	private final String beanName = "Attribute";
	@NonNull private String baseFriendlyName;
	@NonNull private String friendlyNameParameter;

	public PerunAttributeDefinition(Long id, String friendlyName, String namespace, String description, String type,
									String displayName, boolean writable, boolean unique, String entity,
									String baseFriendlyName, String friendlyNameParameter) {
		super(id);
		this.setFriendlyName(friendlyName);
		this.setNamespace(namespace);
		this.setDescription(description);
		this.setType(type);
		this.setDisplayName(displayName);
		this.setWritable(writable);
		this.setUnique(unique);
		this.setEntity(entity);
		this.setBaseFriendlyName(baseFriendlyName);
		this.setFriendlyNameParameter(friendlyNameParameter);
	}


	public JsonNode toJson() {
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put(MapperUtils.ID, super.getId());
		res.put(MapperUtils.FRIENDLY_NAME, friendlyName);
		res.put(MapperUtils.NAMESPACE, namespace);
		res.put(MapperUtils.DESCRIPTION, description);
		res.put(MapperUtils.TYPE, type);
		res.put(MapperUtils.DISPLAY_NAME, displayName);
		res.put(MapperUtils.WRITABLE, writable);
		res.put(MapperUtils.ENTITY, entity);
		res.put(MapperUtils.BEAN_NAME, beanName);
		res.put(MapperUtils.UNIQUE, unique);
		res.put(MapperUtils.BASE_FRIENDLY_NAME, baseFriendlyName);
		res.put(MapperUtils.FRIENDLY_NAME_PARAMETER, friendlyNameParameter);

		return res;
	}

	public void setFriendlyName(String friendlyName) {
		if (!StringUtils.hasText(friendlyName)) {
			throw new IllegalArgumentException("FriendlyName cannot be empty!");
		}
		this.friendlyName = friendlyName;
	}

	public void setNamespace(String namespace) {
		if (!StringUtils.hasText(namespace)) {
			throw new IllegalArgumentException("Namespace cannot be empty!");
		}
		this.namespace = namespace;
	}

	public void setDescription(String description) {
		if (!StringUtils.hasText(description)) {
			throw new IllegalArgumentException("Description cannot be empty!");
		}
		this.description = description;
	}

	public void setType(String type) {
		if (!StringUtils.hasText(type)) {
			throw new IllegalArgumentException("Type cannot be empty!");
		}
		this.type = type;
	}

	public void setDisplayName(String displayName) {
		if (!StringUtils.hasText(displayName)) {
			throw new IllegalArgumentException("DisplayName cannot be empty!");
		}
		this.displayName = displayName;
	}

	public void setEntity(String entity) {
		if (!StringUtils.hasText(entity)) {
			throw new IllegalArgumentException("Entity cannot be empty!");
		}
		this.entity = entity;
	}

	public void setBaseFriendlyName(String baseFriendlyName) {
		if (!StringUtils.hasText(baseFriendlyName)) {
			throw new IllegalArgumentException("BaseFriendlyName cannot be empty!");
		}
		this.baseFriendlyName = baseFriendlyName;
	}

	public void setFriendlyNameParameter(String friendlyNameParameter) {
		if (!StringUtils.hasText(friendlyNameParameter)) {
			throw new IllegalArgumentException("FriendlyNameParameter cannot be empty!");
		}
		this.friendlyNameParameter = friendlyNameParameter;
	}

	@JsonIgnore
	public String getFullName() {
		return this.namespace + ':' + this.friendlyName;
	}

}
