package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Generic configuration class containing all other configuration classes.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@Slf4j
public class Config {

	private AttrsConfig facilityServiceConfig;
	private AttrsConfig facilityOrganizationConfig;
	private AttrsConfig facilityMembershipConfig;
	private AttrsConfig facilityOidcConfig;
	private AttrsConfig facilitySamlConfig;
	private Properties messagesConfig;
	private final Map<String, AttrInput> inputMap = new HashMap<>();

	public void setFacilityServiceConfig(AttrsConfig facilityServiceConfig) {
		this.facilityServiceConfig = facilityServiceConfig;
		for (AttrInput a: facilityServiceConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityOrganizationConfig(AttrsConfig facilityOrganizationConfig) {
		this.facilityOrganizationConfig = facilityOrganizationConfig;
		for (AttrInput a: facilityOrganizationConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityMembershipConfig(AttrsConfig facilityMembershipConfig) {
		this.facilityMembershipConfig = facilityMembershipConfig;
		for (AttrInput a: facilityMembershipConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityOidcConfig(AttrsConfig facilityOidcConfig) {
		this.facilityOidcConfig = facilityOidcConfig;
		for (AttrInput a: facilityOidcConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilitySamlConfig(AttrsConfig facilitySamlConfig) {
		this.facilitySamlConfig = facilitySamlConfig;
		for (AttrInput a: facilitySamlConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public List<AttrInput> getServiceInputs() {
		return this.facilityServiceConfig.getInputs();
	}

	public List<AttrInput> getOrganizationInputs() {
		return this.facilityOrganizationConfig.getInputs();
	}

	public List<AttrInput> getSamlInputs() {
		return this.facilitySamlConfig.getInputs();
	}

	public List<AttrInput> getOidcInputs() {
		return this.facilityOidcConfig.getInputs();
	}

	public List<AttrInput> getMembershipInputs() {
		return this.facilityMembershipConfig.getInputs();
	}

}
