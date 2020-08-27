package cz.metacentrum.perun.spRegistration.rest.controllers.common;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.ApprovalsProperties;
import cz.metacentrum.perun.spRegistration.common.configs.Config;
import cz.metacentrum.perun.spRegistration.common.configs.FrontendProperties;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Controller handling requests for obtaining configuration
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class ConfigController {

	private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

	private final Config config;
	private final ApplicationProperties applicationProperties;
	private final FrontendProperties frontendProperties;
	private final ApprovalsProperties approvalsProperties;

	@Autowired
	public ConfigController(Config config,
							ApplicationProperties applicationProperties,
							FrontendProperties frontendProperties,
							ApprovalsProperties approvalsProperties) {
		this.config = config;
		this.applicationProperties = applicationProperties;
		this.frontendProperties = frontendProperties;
		this.approvalsProperties = approvalsProperties;
	}

	@GetMapping(path = "/api/config/oidcInputs")
	public List<List<AttrInput>> getInputsForOidc() {
		log.trace("getInputsForOidc()");
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(config.getServiceInputs());
		inputs.add(config.getOrganizationInputs());
		inputs.add(config.getOidcInputs());
		inputs.add(config.getMembershipInputs());

		log.trace("getInputsForOidc() returns: {}", inputs);
		return inputs;
	}

	@GetMapping(path = "/api/config/samlInputs")
	public List<List<AttrInput>> getInputsForSaml() {
		log.trace("getInputsForSaml()");
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(config.getServiceInputs());
		inputs.add(config.getOrganizationInputs());
		inputs.add(config.getSamlInputs());
		inputs.add(config.getMembershipInputs());

		log.trace("getInputsForSaml() returns: {}", inputs);
		return inputs;
	}

	@GetMapping(path = "/api/config/protocols")
	public String[] getProtocolsEnabled() {
		return applicationProperties.getProtocolsEnabled().toArray(new String[] {});
	}

	@GetMapping(path = "/api/config/langs")
	public List<String> getLangs() {
		Set<String> langs = applicationProperties.getLanguagesEnabled();

		log.trace("getAvailableLanguages() returns: {}", langs);
		return new ArrayList<>(langs);
	}

	@GetMapping(path = "/api/config/isUserAdmin")
	public boolean isUserAdmin(@SessionAttribute("user") User user) {
		boolean isAdmin = applicationProperties.isAppAdmin(user.getId());

		log.trace("isUserAdmin({}) returns: {}", user, isAdmin);
		return isAdmin;
	}

	@GetMapping(path = "/api/config/pageConfig")
	public Map<String, String> getPageConfig() {
		log.trace("getPageConfig()");
		Map<String, String> pageConfig = new HashMap<>();
		pageConfig.put("logoUrl", frontendProperties.getHeaderLogoUrl());
		pageConfig.put("headerLabel", frontendProperties.getHeaderTitle());
		pageConfig.put("footerHtml", frontendProperties.getFooterHtml());
		pageConfig.put("headerHtml", frontendProperties.getHeaderHtml());
		pageConfig.put("logoutUrl", applicationProperties.getLogoutUrl());

		log.trace("getPageConfig() returns: {}", pageConfig);
		return pageConfig;
	}

	@GetMapping(path = "/api/config/specifyAuthoritiesEnabled")
	public boolean getSpecifyAuthoritiesEnabled() {
		boolean specifyAuthoritiesEnabled = approvalsProperties.isSpecifyOwn();

		log.trace("getSpecifyAuthoritiesEnabled() returns: {}", specifyAuthoritiesEnabled);
		return specifyAuthoritiesEnabled;
	}

	@GetMapping(path = "/api/config/prodTransferEntries")
	public Set<String> getProdTransferEntries() {
		log.trace("getProdTransferEntries()");

		Set<String> entries = approvalsProperties.getTransferAuthoritiesMap().keySet();

		log.trace("getProdTransferEntries() returns: {}", entries);
		return entries;
	}
}
