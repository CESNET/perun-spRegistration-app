package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Input for attribute. Holds configuration of inputs like if it is required, should be displayed etc.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttrInput {

	@NotBlank private String name;
	@NotEmpty private Map<String, String> displayName;
	@NotEmpty private Map<String, String> description;
	private boolean required = true;
	private boolean displayed = true;
	private boolean editable = true;
	@NotBlank private String type;
	private List<String> allowedValues = new ArrayList<>();
	private int displayPosition;
	private String regex;
	private boolean encrypted = false;
	private List<String> allowedKeys = new ArrayList<>(); // only if the type is Map

}
