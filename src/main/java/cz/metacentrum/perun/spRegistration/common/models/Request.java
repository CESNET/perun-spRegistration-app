package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.common.SpregUtils;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class represents request made by user. It contains all the data that needs to be stored.
 * It also keeps track of the modification time.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString(exclude = {"attributes", "requester"})
@EqualsAndHashCode(exclude = {"attributes", "requester"})
public class Request {

	@NonNull private Long reqId;
	private Long facilityId;
	private Long reqUserId;
	private User requester;
	@NonNull private RequestAction action;
	@NonNull private RequestStatus status;
	private Map<AttributeCategory, Map<String, PerunAttribute>> attributes = new HashMap<>();
	private Timestamp modifiedAt;
	private Long modifiedBy;
	private User modifier;

	@JsonIgnore
	public Map<String, String> getFacilityName(String attrName) {
		if (attributes.containsKey(AttributeCategory.SERVICE)) {
			Map<String, PerunAttribute> serviceAttrs = attributes.get(AttributeCategory.SERVICE);
			if (serviceAttrs.containsKey(attrName)) {
				PerunAttribute attr = serviceAttrs.get(attrName);
				if (attr != null) {
					return attr.valueAsMap();
				}
			}
		}

		return new HashMap<>();
	}

	@JsonIgnore
	public Map<String, String> getFacilityDescription(String attrName) {
		if (attributes.containsKey(AttributeCategory.SERVICE)) {
			Map<String, PerunAttribute> serviceAttrs = attributes.get(AttributeCategory.SERVICE);
			if (serviceAttrs.containsKey(attrName)) {
				PerunAttribute attr = serviceAttrs.get(attrName);
				if (attr != null) {
					return attr.valueAsMap();
				}
			}
		}

		return new HashMap<>();
	}

	/**
	 * Convert attributes to JSON format suitable for storing into DB.
	 * @return JSON with attributes.
	 */
	@JsonIgnore
	public String getAttributesAsJsonForDb(AppBeansContainer appBeansContainer) {
		if (this.attributes == null || this.attributes.isEmpty()) {
			return "";
		}

		ObjectNode root = JsonNodeFactory.instance.objectNode();
		for (Map.Entry<AttributeCategory, Map<String, PerunAttribute>> categoryMapEntry : attributes.entrySet()) {
			ObjectNode obj = JsonNodeFactory.instance.objectNode();
			AttributeCategory category = categoryMapEntry.getKey();
			Map<String, PerunAttribute> attributeMap = categoryMapEntry.getValue();
			for (Map.Entry<String, PerunAttribute> a : attributeMap.entrySet()) {
				PerunAttributeDefinition def = appBeansContainer.getAttrDefinition(a.getKey());
				PerunAttribute attribute = a.getValue();
				attribute.setDefinition(def);
				obj.set(a.getKey(), attribute.toJsonForDb());
			}
			root.set(category.toString(), obj);
		}

		return root.toString();
	}

	/**
	 * Convert attributes to JSON format suitable for storing into Perun.
	 * @return JSON with attributes or null.
	 */
	@JsonIgnore
	public ArrayNode getAttributesAsJsonArrayForPerun() {
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		ArrayNode res = JsonNodeFactory.instance.arrayNode();
		attributes.values().forEach(e -> e.values().forEach(a -> res.add(a.toJson())));

		return res;
	}

	/**
	 * Extract administrator contact from attributes
	 * @param attrKey name of attribute containing administrator contact
	 * @return Administrator contact
	 */
	public String getAdminContact(String attrKey) {
		if (attributes != null && attributes.containsKey(AttributeCategory.SERVICE) &&
				attributes.get(AttributeCategory.SERVICE).containsKey(attrKey)
				&& attributes.get(AttributeCategory.SERVICE).get(attrKey) != null) {
			return attributes.get(AttributeCategory.SERVICE).get(attrKey).valueAsString();
		}

		return null;
	}

	public void updateAttributes(List<PerunAttribute> attrsToUpdate, boolean clearComment, AppBeansContainer appBeansContainer) {
		if (attrsToUpdate == null) {
			return;
		}

		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}

		for (PerunAttribute attr: attrsToUpdate) {
			AttributeCategory category = appBeansContainer.getAttrCategory(attr.getFullName());
			if (!this.attributes.containsKey(category)) {
				this.attributes.put(category, new HashMap<>());
			}
			Map<String, PerunAttribute> categoryAttrsMap = this.attributes.get(category);
			if (categoryAttrsMap.containsKey(attr.getFullName())) {
				PerunAttribute old = categoryAttrsMap.get(attr.getFullName());
				old.setValue(old.getDefinition().getType(), attr.getValue());
				old.setComment(clearComment ? null : attr.getComment());
			} else {
				categoryAttrsMap.put(attr.getFullName(), attr);
				if (clearComment) {
					attr.setComment(null);
				}
			}
		}
		this.attributes = SpregUtils.filterInvalidAttributes(attributes, appBeansContainer.getAttributeDefinitionMap());
	}

	@JsonIgnore
	public List<String> getAttributeNames() {
		Set<String> res = new HashSet<>();

		if (this.attributes != null && !this.attributes.isEmpty()) {
			this.attributes.values().forEach(
					e -> e.values().forEach(a -> res.add(a.getFullName()))
			);
		}

		return new ArrayList<>(res);
	}

}
