package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class represents request made by user. It contains all the data that needs to be stored.
 * It also keeps track of the modification time.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class Request {

	private Long reqId;
	private Long facilityId;
	private User requester;
	private RequestStatus status;
	private RequestAction action;
	private Long reqUserId;
	private Map<AttributeCategory, Map<String, PerunAttribute>> attributes = new HashMap<>();
	private Timestamp modifiedAt;
	private Long modifiedBy;
	private User modifier;

	public Long getReqId() {
		return reqId;
	}

	public void setReqId(Long reqId) {
		this.reqId = reqId;
	}

	public Long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public RequestAction getAction() {
		return action;
	}

	public void setAction(RequestAction action) {
		this.action = action;
	}

	public Long getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(Long reqUserId) {
		this.reqUserId = reqUserId;
	}

	public Map<AttributeCategory, Map<String, PerunAttribute>> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<AttributeCategory, Map<String, PerunAttribute>> attributes) {
		this.attributes = attributes;
	}

	public Timestamp getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Timestamp modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public User getModifier() {
		return modifier;
	}

	public void setModifier(User modifier) {
		this.modifier = modifier;
	}

	@JsonIgnore
	public Map<String, String> getFacilityName(String attrName) {
		PerunAttribute attr = attributes.get(AttributeCategory.SERVICE).get(attrName);
		if (attr == null) {
			return new HashMap<>();
		} else {
			return attr.valueAsMap();
		}
	}

	@JsonIgnore
	public Map<String, String> getFacilityDescription(String attrName) {
		PerunAttribute attr = attributes.get(AttributeCategory.SERVICE).get(attrName);
		if (attr == null) {
			return new HashMap<>();
		} else {
			return attr.valueAsMap();
		}
	}

	/**
	 * Convert attributes to JSON format suitable for storing into DB.
	 * @return JSON with attributes.
	 */
	@JsonIgnore
	public String getAttributesAsJsonForDb(AppConfig appConfig) {
		if (this.attributes == null || this.attributes.isEmpty()) {
			return "";
		}

		ObjectNode root = JsonNodeFactory.instance.objectNode();
		for (Map.Entry<AttributeCategory, Map<String, PerunAttribute>> categoryMapEntry : attributes.entrySet()) {
			ObjectNode obj = JsonNodeFactory.instance.objectNode();
			AttributeCategory category = categoryMapEntry.getKey();
			Map<String, PerunAttribute> attributeMap = categoryMapEntry.getValue();
			for (Map.Entry<String, PerunAttribute> a : attributeMap.entrySet()) {
				PerunAttributeDefinition def = appConfig.getAttrDefinition(a.getKey());
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

	@Override
	public String toString() {
		return "Request{" +
				"reqId=" + reqId +
				", facilityId=" + facilityId +
				", status=" + status +
				", action=" + action +
				", reqUserId=" + reqUserId +
				", attributes=" + attributes +
				", modifiedAt=" + modifiedAt +
				", modifiedBy=" + modifiedBy +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Request request = (Request) o;
		return Objects.equals(reqId, request.reqId) &&
				status == request.status &&
				action == request.action &&
				Objects.equals(reqUserId, request.reqUserId);
	}

	@Override
	public int hashCode() {
		long res = 31 * reqId;
		res *= 31 * action.hashCode();
		res *= 31 * status.hashCode();
		if (facilityId!= null) res *= 31 * facilityId;

		return (int) res;
	}

	public void updateAttributes(List<PerunAttribute> attrsToUpdate, boolean clearComment, AppConfig appConfig) {
		if (attrsToUpdate == null) {
			return;
		}

		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}

		for (PerunAttribute attr: attrsToUpdate) {
			AttributeCategory category = appConfig.getAttrCategory(attr.getFullName());
			if (!this.attributes.containsKey(category)) {
				this.attributes.put(category, new HashMap<>());
			}
			Map<String, PerunAttribute> categoryAttrsMap = this.attributes.get(category);
			if (categoryAttrsMap.containsKey(attr.getFullName())) {
				PerunAttribute old = categoryAttrsMap.get(attr.getFullName());
				old.setValue(attr.getValue());
				old.setComment(clearComment ? null : attr.getComment());
			} else {
				categoryAttrsMap.put(attr.getFullName(), attr);
				if (clearComment) {
					attr.setComment(null);
				}
			}
		}
		this.attributes = appConfig.filterInvalidAttributes(attributes);
	}

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
