package cz.metacentrum.perun.spRegistration.persistence.models;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Representation of Perun User.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class User extends PerunEntity {

	private String titleBefore;
	private String firstName;
	private String middleName;
	private String lastName;
	private String titleAfter;
	private String email;
	private List<Long> facilitiesWhereAdmin;
	private boolean isAdmin;

	public User(Long id, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) {
		super(id);
		this.titleBefore = titleBefore;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.titleAfter = titleAfter;
	}

	public String getTitleBefore() {
		return titleBefore;
	}

	public void setTitleBefore(String titleBefore) {
		this.titleBefore = titleBefore;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTitleAfter() {
		return titleAfter;
	}

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Long> getFacilitiesWhereAdmin() {
		return facilitiesWhereAdmin;
	}

	public void setFacilitiesWhereAdmin(List<Long> facilitiesWhereAdmin) {
		this.facilitiesWhereAdmin = facilitiesWhereAdmin;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}

	public String getName() {
		return firstName + middleName + lastName;
	}

	public static User fromPerunJson(JSONObject json) {
		Long id = json.getLong("id");
		String firstName = json.optString("firstName", "");
		String middleName = json.optString("middleName", "");
		String lastName = json.optString("lastName", "");
		String titleBefore = json.optString("titleBefore", "");
		String titleAfter = json.optString("titleAfter", "");

		return new User(id, firstName, middleName, lastName, titleBefore, titleAfter);
	}

	public String getFullName() {
		StringJoiner joiner = new StringJoiner(" ");
		if (titleBefore != null && !titleBefore.isEmpty()) {
			joiner.add(titleBefore);
		}
		if (firstName != null && !firstName.isEmpty()) {
			joiner.add(firstName);
		}
		if (middleName != null && !middleName.isEmpty()) {
			joiner.add(middleName);
		}
		if (lastName != null && !lastName.isEmpty()) {
			joiner.add(lastName);
		}
		if (titleAfter != null && !titleAfter.isEmpty()) {
			joiner.add(titleAfter);
		}

		return joiner.toString();
	}

	@Override
	public String toString() {
		return "User{" +
				super.toString() +
				", titleBefore='" + titleBefore + '\'' +
				", firstName='" + firstName + '\'' +
				", middleName='" + middleName + '\'' +
				", lastName='" + lastName + '\'' +
				", titleAfter='" + titleAfter + '\'' +
				", email='" + email + '\'' +
				", facilitiesWhereAdmin=" + facilitiesWhereAdmin +
				", isAdmin=" + isAdmin +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof User)) {
			return false;
		}

		User them = (User) o;
		return Objects.equals(this.getId(), them.getId());
	}

	@Override
	public int hashCode() {
		long res = 31 * this.getId();

		return (int) res;
	}
}
