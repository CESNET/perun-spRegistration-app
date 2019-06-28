package cz.metacentrum.perun.spRegistration.persistence.configs;

import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Generic configuration class containing all other configuration classes.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class Config {

	private static final Logger log = LoggerFactory.getLogger(Config.class);

	private AppConfig appConfig;
	private AttrsConfig facilityServiceConfig;
	private AttrsConfig facilityOrganizationConfig;
	private AttrsConfig facilityMembershipConfig;
	private AttrsConfig facilityOidcConfig;
	private AttrsConfig facilitySamlConfig;
	private Properties messagesConfig;
	private Map<String, AttrInput> inputMap = new HashMap<>();

	public void setAppConfig(AppConfig appConfig) {
		log.debug("setting app config: {}", appConfig);
		this.appConfig = appConfig;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setFacilityServiceConfig(AttrsConfig facilityServiceConfig) {
		log.trace("setting facility service config: {}", facilityServiceConfig);
		this.facilityServiceConfig = facilityServiceConfig;
		for (AttrInput a: facilityServiceConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityOrganizationConfig(AttrsConfig facilityOrganizationConfig) {
		log.trace("setting facility organization config: {}", facilityOrganizationConfig);
		this.facilityOrganizationConfig = facilityOrganizationConfig;
		for (AttrInput a: facilityOrganizationConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityMembershipConfig(AttrsConfig facilityMembershipConfig) {
		log.trace("setting facility membership config: {}", facilityMembershipConfig);
		this.facilityMembershipConfig = facilityMembershipConfig;
		for (AttrInput a: facilityMembershipConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilityOidcConfig(AttrsConfig facilityOidcConfig) {
		log.trace("setting facility oidc config: {}", facilityOidcConfig);
		this.facilityOidcConfig = facilityOidcConfig;
		for (AttrInput a: facilityOidcConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setFacilitySamlConfig(AttrsConfig facilitySamlConfig) {
		log.trace("setting facility saml config: {}", facilitySamlConfig);
		this.facilitySamlConfig = facilitySamlConfig;
		for (AttrInput a: facilitySamlConfig.getInputs()) {
			inputMap.put(a.getName(), a);
		}
	}

	public void setMessagesConfig(Properties messagesConfig) {
		log.trace("setting messages config: {}", messagesConfig);
		this.messagesConfig = messagesConfig;
	}

	public void setInputMap(Map<String, AttrInput> inputMap) {
		this.inputMap = inputMap;
	}

	public Properties getMessagesConfig() {
		return messagesConfig;
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

	public Map<String, AttrInput> getInputMap() {
		return inputMap;
	}

}
