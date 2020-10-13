package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.ApprovalsProperties;
import cz.metacentrum.perun.spRegistration.common.configs.FrontendProperties;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ConfigController {

	@NonNull private final ApplicationProperties applicationProperties;
	@NonNull private final FrontendProperties frontendProperties;
	@NonNull private final ApprovalsProperties approvalsProperties;
	@NonNull private final List<AttrInput> serviceInputs;
	@NonNull private final List<AttrInput> organizationInputs;
	@NonNull private final List<AttrInput> membershipInputs;
	@NonNull private final List<AttrInput> oidcInputs;
	@NonNull private final List<AttrInput> samlInputs;

	@Autowired
	public ConfigController(@NonNull ApplicationProperties applicationProperties,
							@NonNull FrontendProperties frontendProperties,
							@NonNull ApprovalsProperties approvalsProperties,
							@NonNull InputsContainer inputsContainer)
	{
		this.applicationProperties = applicationProperties;
		this.frontendProperties = frontendProperties;
		this.approvalsProperties = approvalsProperties;
		this.serviceInputs = inputsContainer.getServiceInputs();
		this.organizationInputs = inputsContainer.getOrganizationInputs();
		this.membershipInputs = inputsContainer.getMembershipInputs();
		this.oidcInputs = inputsContainer.getOidcInputs();
		this.samlInputs = inputsContainer.getSamlInputs();
	}

	@GetMapping(path = "/api/config/oidcInputs")
	public List<List<AttrInput>> getInputsForOidc() {
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(serviceInputs);
		inputs.add(organizationInputs);
		inputs.add(oidcInputs);
		inputs.add(membershipInputs);
		return inputs;
	}

	@GetMapping(path = "/api/config/samlInputs")
	public List<List<AttrInput>> getInputsForSaml() {
		List<List<AttrInput>> inputs = new ArrayList<>();
		inputs.add(serviceInputs);
		inputs.add(organizationInputs);
		inputs.add(samlInputs);
		inputs.add(membershipInputs);
		return inputs;
	}

	@GetMapping(path = "/api/config/protocols")
	public String[] getProtocolsEnabled() {
		return applicationProperties.getProtocolsEnabled().toArray(new String[] {});
	}

	@GetMapping(path = "/api/config/langs")
	public List<String> getLangs() {
		return new ArrayList<>(applicationProperties.getLanguagesEnabled());
	}

	@GetMapping(path = "/api/config/isUserAdmin")
	public boolean isUserAdmin(@NonNull @SessionAttribute("user") User user) {
		return applicationProperties.isAppAdmin(user.getId());
	}

	@GetMapping(path = "/api/config/pageConfig")
	public Map<String, String> getPageConfig() {
		Map<String, String> pageConfig = new HashMap<>();
		pageConfig.put("logoUrl", frontendProperties.getHeaderLogoUrl());
		pageConfig.put("headerLabel", frontendProperties.getHeaderTitle());
		pageConfig.put("footerHtml", frontendProperties.getFooterHtml());
		pageConfig.put("headerHtml", frontendProperties.getHeaderHtml());
		pageConfig.put("logoutUrl", applicationProperties.getLogoutUrl());
		return pageConfig;
	}

	@GetMapping(path = "/api/config/specifyAuthoritiesEnabled")
	public boolean getSpecifyAuthoritiesEnabled() {
		return approvalsProperties.isSpecifyOwn();
	}

	@GetMapping(path = "/api/config/prodTransferEntries")
	public Set<String> getProdTransferEntries() {
		return approvalsProperties.getTransferAuthoritiesMap().keySet();
	}
}
