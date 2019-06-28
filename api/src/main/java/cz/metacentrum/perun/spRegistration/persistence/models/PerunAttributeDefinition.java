package cz.metacentrum.perun.spRegistration.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.metacentrum.perun.spRegistration.Utils;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Attribute definition from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class PerunAttributeDefinition extends PerunEntity {

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

	public static PerunAttributeDefinition fromPerunJson(JSONObject jsonObject) {
		if (Utils.checkParamsInvalid(jsonObject)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Long id = jsonObject.getLong("id");
		String friendlyName = jsonObject.getString("friendlyName");
		String namespace = jsonObject.getString("namespace");
		String description = jsonObject.getString("description");
		String type = jsonObject.getString("type");
		String displayName = jsonObject.getString("displayName");
		boolean writable = jsonObject.getBoolean("writable");
		boolean unique = jsonObject.getBoolean("unique");
		String entity = jsonObject.getString("entity");
		String baseFriendlyName = jsonObject.getString("baseFriendlyName");
		String friendlyNameParameter = jsonObject.getString("friendlyNameParameter");

		return new PerunAttributeDefinition(id, friendlyName, namespace, description, type, displayName, writable,
				unique, entity, baseFriendlyName, friendlyNameParameter);
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public String getBaseFriendlyName() {
		return baseFriendlyName;
	}

	public void setBaseFriendlyName(String baseFriendlyName) {
		this.baseFriendlyName = baseFriendlyName;
	}

	public String getFriendlyNameParameter() {
		return friendlyNameParameter;
	}

	public void setFriendlyNameParameter(String friendlyNameParameter) {
		this.friendlyNameParameter = friendlyNameParameter;
	}

	public JSONObject toJson() {
		JSONObject res = new JSONObject();
		res.put("id", super.getId());
		res.put("friendlyName", friendlyName);
		res.put("namespace", namespace);
		res.put("description", description);
		res.put("type", type);
		res.put("displayName", displayName);
		res.put("writable", writable);
		res.put("entity", entity);
		res.put("beanName", beanName);
		res.put("unique", unique);
		res.put("baseFriendlyName", baseFriendlyName);
		res.put("friendlyNameParameter", friendlyNameParameter);

		return res;
	}

	@JsonIgnore
	public String getFullName() {
		return this.namespace + ':' + this.friendlyName;
	}

	@Override
	public String toString() {
		return "PerunAttributeDefinition{" +
				"friendlyName='" + friendlyName + '\'' +
				", namespace='" + namespace + '\'' +
				", description='" + description + '\'' +
				", type='" + type + '\'' +
				", displayName='" + displayName + '\'' +
				", writable=" + writable +
				", unique=" + unique +
				", entity='" + entity + '\'' +
				", beanName='" + beanName + '\'' +
				", baseFriendlyName='" + baseFriendlyName + '\'' +
				", friendlyNameParameter='" + friendlyNameParameter + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof PerunAttributeDefinition)) {
			return false;
		}

		PerunAttributeDefinition them = (PerunAttributeDefinition) o;
		return Objects.equals(this.getId(), them.getId())
				&& Objects.equals(this.friendlyName, them.friendlyName)
				&& Objects.equals(this.namespace, them.namespace)
				&& Objects.equals(this.entity, them.entity);
	}

	@Override
	public int hashCode() {
		long res = 31 * this.getId();
		res *= 31 * friendlyName.hashCode();
		res *= 31 * namespace.hashCode();
		res *= 31 * entity.hashCode();

		return (int) res;
	}

}
