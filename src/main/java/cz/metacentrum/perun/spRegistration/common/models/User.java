package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

	@NonNull private String name;
	private String email;

	@JsonProperty("isAppAdmin")
	private boolean isAppAdmin = false;

	@JsonIgnore
	private List<Long> facilitiesWhereAdmin = new ArrayList<>();

	public User(Long id) {
		super(id);
	}

	public User(Long id, String name, String email, List<Long> facilitiesWhereAdmin, boolean isAppAdmin) {
		super(id);
		this.setName(name);
		this.setEmail(email);
		this.setFacilitiesWhereAdmin(facilitiesWhereAdmin);
		this.setAppAdmin(isAppAdmin);
	}

	public void setName(String name) {
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name cannot be empty");
		}

		this.name = name;
	}

}
