package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.persistence.configs.Config;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GeneralController {

	private static final Logger log = LoggerFactory.getLogger(GeneralController.class);

	private final Config config;

	@Autowired
	public GeneralController(Config config) {
		this.config = config;
	}

	@RequestMapping(path = "/api/config/oidcInputs")
	public List<List<AttrInput>> getInputsForOidc() {
		log.debug("getInputsForOidc()");
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(config.getServiceInputs());
		inputs.add(config.getOrganizationInputs());
		inputs.add(config.getOidcInputs());
		inputs.add(config.getMembershipInputs());
		return inputs;
	}

	@RequestMapping(path = "/api/config/samlInputs")
	public List<List<AttrInput>> getInputsForSaml() {
		log.debug("getInputsForSaml()");
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(config.getServiceInputs());
		inputs.add(config.getOrganizationInputs());
		inputs.add(config.getSamlInputs());
		inputs.add(config.getMembershipInputs());
		return inputs;
	}

	@RequestMapping(path = "/api/config/oidcEnabled")
	public boolean getOidcEnabled() {
		log.debug("getOidcEnabled()");
		return config.getAppConfig().isOidcEnabled();
	}

	@RequestMapping(path = "/api/config/langs")
	public List<String> getLangs() {
		log.debug("getLangs()");
		return config.getAppConfig().getLangs();
	}

	@RequestMapping(path = "/api/config/isUserAdmin")
	public boolean isUserAdmin(@SessionAttribute("userId") Long userId) {
		log.debug("isUserAdmin()");
		return config.getAppConfig().isAdmin(userId);
	}

	@RequestMapping(path = "/api/config/footer", method = RequestMethod.GET)
	public String getAppFooter() {
		log.debug("getAppFooter()");
		return config.getAppConfig().getFooterHTML();
	}

	@RequestMapping(path = "/api/config/logo", method = RequestMethod.GET)
	public String getHeaderLogo() {
		log.debug("getHeaderLogo()");
		return config.getAppConfig().getHeaderLogo();
	}

	@RequestMapping(path = "/api/config/headerLabel", method = RequestMethod.GET)
	public String getHeaderLabel() {
		log.debug("getHeaderLabel()");
		return config.getAppConfig().getHeaderTitle();
	}

	@RequestMapping(path = "/api/config/header", method = RequestMethod.GET)
	public String getHeader() {
		log.debug("getHeader()");
		return config.getAppConfig().getHeaderHTML();
	}

	@RequestMapping(path = "/api/config/specifyAuthoritiesEnabled", method = RequestMethod.GET)
	public boolean getSpecifyAuthoritiesEnabled() {
		log.debug("getSpecifyAuthoritiesEnabled()");
		return config.getAppConfig().getSpecifyAuthoritiesEnabled();
	}
}
