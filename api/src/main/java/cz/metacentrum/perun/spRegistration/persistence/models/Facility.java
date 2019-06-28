package cz.metacentrum.perun.spRegistration.persistence.models;

import cz.metacentrum.perun.spRegistration.Utils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of Perun Facility.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class Facility extends PerunEntity {

	private String name;
	private String description;
	private boolean testEnv;
	private Long activeRequestId;
	private Map<String, PerunAttribute> attrs = new HashMap<>();

	public Facility(Long id) {
		super(id);
	}

	public Facility(Long id, String name, String description) {
		super(id);
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isTestEnv() {
		return testEnv;
	}

	public void setTestEnv(boolean testEnv) {
		this.testEnv = testEnv;
	}

	public Long getActiveRequestId() {
		return activeRequestId;
	}

	public void setActiveRequestId(Long activeRequestId) {
		this.activeRequestId = activeRequestId;
	}

	public Map<String, PerunAttribute> getAttrs() {
		return attrs;
	}

	public void setAttrs(Map<String, PerunAttribute> attrs) {
		this.attrs = attrs;
	}

	/**
	 * Convert object to JSON representation
	 * @return JSON String
	 */
	public JSONObject toJson() {
		JSONObject res = new JSONObject();
		if (this.getId() == null) {
			res.put("id", JSONObject.NULL);
		} else {
			res.put("id", getId());
		}
		res.put("name", name);
		res.put("description", description);
		res.put("beanName", "facility");

		return res;
	}

	/**
	 * Convert JSON from perun to facility object
	 * @param json JSON from Perun
	 * @return Facility object or null
	 */
	public static Facility fromPerunJson(JSONObject json) {
		if (Utils.checkParamsInvalid(json)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		Long id = json.getLong("id");
		String name = json.getString("name");
		String description = json.optString("description", "");

		return new Facility(id, name, description);
	}

	@Override
	public String toString() {
		return "Facility{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", testEnv=" + testEnv +
				", activeRequestId=" + activeRequestId +
				", attrs=" + attrs +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Facility facility = (Facility) o;
		return testEnv == facility.testEnv &&
				Objects.equals(name, facility.name) &&
				Objects.equals(description, facility.description) &&
				Objects.equals(attrs, facility.attrs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, testEnv, attrs);
	}
}
