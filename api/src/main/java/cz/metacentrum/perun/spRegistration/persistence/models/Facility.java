package cz.metacentrum.perun.spRegistration.persistence.models;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of Perun Facility.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class Facility extends PerunEntity {

	private String name;
	private String description;
	private boolean testEnv;
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

	public static Facility fromPerunJson(JSONObject json) {
		Long id = json.getLong("id");
		String name = json.getString("name");
		String description = json.optString("description", "");

		return new Facility(id, name, description);
	}

	@Override
	public String toString() {
		return "Facility{" +
				super.toString() +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", testEnv='" + testEnv + '\'' +
				", attrs=" + attrs +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Facility)) {
			return false;
		}

		Facility them = (Facility) o;
		return Objects.equals(this.getId(), them.getId())
				&& Objects.equals(this.name, them.name)
				&& Objects.equals(this.description, them.description);
	}

	@Override
	public int hashCode() {
		long res = 31 * this.getId();
		res *= 31 * name.hashCode();
		res *= 31 * description.hashCode();

		return (int) res;
	}
}
