package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Representation of Perun User.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class User extends PerunEntity {

	public static final String KEY_ID = "id";
	public static final String KEY_FIRST_NAME = "firstName";
	public static final String KEY_LAST_NAME = "lastName";
	public static final String KEY_MIDDLE_NAME = "middleName";
	public static final String KEY_TITLE_BEFORE = "titleBefore";
	public static final String KEY_TITLE_AFTER = "titleAfter";
	public static final String IS_APP_ADMIN = "isAppAdmin";

	@NotBlank private String name;
	private String email;
	@JsonIgnore private List<Long> facilitiesWhereAdmin = new ArrayList<>();
	private boolean isAppAdmin = false;

	public User(Long id) {
		super(id);
	}

	public User(Long id, String name, String email, List<Long> facilitiesWhereAdmin, boolean isAppAdmin) {
		super(id);
		this.name = name;
		this.email = email;
		this.facilitiesWhereAdmin = facilitiesWhereAdmin;
		this.isAppAdmin = isAppAdmin;
	}

	/**
	 * Parse user from JSON obtained from Perun
	 * @param json JSON from Perun
	 * @return User or null
	 */
	public static User fromPerunJson(@NotNull JsonNode json) {
		if (json.isNull()) {
			throw new IllegalArgumentException("Null node passed, json cannot be null");
		}

		Long id = json.get(KEY_ID).asLong();
		String firstName = json.hasNonNull(KEY_FIRST_NAME) ? json.get(KEY_FIRST_NAME).textValue() : null;
		String middleName = json.hasNonNull(KEY_MIDDLE_NAME) ? json.get(KEY_MIDDLE_NAME).textValue() : null;
		String lastName = json.hasNonNull(KEY_LAST_NAME) ? json.get(KEY_LAST_NAME).textValue() : null;
		String titleBefore = json.hasNonNull(KEY_TITLE_BEFORE) ? json.get(KEY_TITLE_BEFORE).textValue() : null;
		String titleAfter = json.hasNonNull(KEY_TITLE_AFTER) ? json.get(KEY_TITLE_AFTER).textValue() : null;

		StringJoiner joiner = new StringJoiner(" ");
		if (StringUtils.hasText(titleBefore)) {
			joiner.add(titleBefore);
		}
		if (StringUtils.hasText(firstName)) {
			joiner.add(firstName);
		}
		if (StringUtils.hasText(middleName)) {
			joiner.add(middleName);
		}
		if (StringUtils.hasText(lastName)) {
			joiner.add(lastName);
		}
		if (StringUtils.hasText(titleAfter)) {
			joiner.add(titleAfter);
		}

		User user = new User(id);
		user.setName(joiner.toString());
		return user;
	}

}
