package cz.metacentrum.perun.spRegistration.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class represents request made by user. It contains all the data that needs to be stored.
 * It also keeps track of the modification time.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class Request {

	private Long reqId;
	private Long facilityId;
	private RequestStatus status;
	private RequestAction action;
	private Long reqUserId;
	private Map<String, PerunAttribute> attributes = new HashMap<>();
	private Timestamp modifiedAt;
	private Long modifiedBy;

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

	public Map<String, PerunAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, PerunAttribute> attributes) {
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

	@JsonIgnore
	public String getFacilityName() {
		PerunAttribute attr = attributes.get("urn:perun:facility:attribute-def:def:serviceName");
		if (attr == null) {
			return null;
		}
		Map<String, String> value = attr.valueAsMap();
		return value.get("en");
	}

	@JsonIgnore
	public String getFacilityDescription() {
		PerunAttribute attr = attributes.get("urn:perun:facility:attribute-def:def:serviceDescription");
		if (attr == null) {
			return null;
		}
		Map<String, String> value = attr.valueAsMap();
		return value.get("en");
	}

	/**
	 * Convert attributes to JSON format suitable for storing into DB.
	 * @return JSON with attributes.
	 */
	@JsonIgnore
	public String getAttributesAsJsonForDb() {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String ,PerunAttribute> a: attributes.entrySet()) {
			obj.put(a.getKey(), a.getValue().toJsonForDb());
		}

		return obj.toString();
	}

	/**
	 * Convert attributes to JSON format suitable for storing into Perun.
	 * @return JSON with attributes or null.
	 */
	@JsonIgnore
	public JSONArray getAttributesAsJsonArrayForPerun() {
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		JSONArray res = new JSONArray();
		for (PerunAttribute a: attributes.values()) {
			res.put(a.toJson());
		}

		return res;
	}

	/**
	 * Extract administrator contact from attributes
	 * @param attrKey name of attribute containing administrator contact
	 * @return Administrator contact
	 */
	public String getAdminContact(String attrKey) {
		if (attributes == null || !attributes.containsKey(attrKey) || attributes.get(attrKey) == null) {
			return null;
		}

		return attributes.get(attrKey).valueAsString();
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

	public void updateAttributes(Map<String, PerunAttribute> attrsToUpdate, boolean clearComment) {
		for (Map.Entry<String, PerunAttribute> entry: attrsToUpdate.entrySet()) {
			if (this.attributes.containsKey(entry.getKey())) {
				PerunAttribute old = this.attributes.get(entry.getKey());
				old.setValue(entry.getValue().getValue());
				old.setComment(clearComment ? null : entry.getValue().getComment());
			} else {
				this.attributes.put(entry.getKey(), entry.getValue());
				if (clearComment) {
					entry.getValue().setComment(null);
				}
			}
		}
	}
}
