package cz.metacentrum.perun.spRegistration.persistence.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for MITREid connection. Contains mapping of attributes.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class MitreIdAttrsConfig {

	private static final Logger log = LoggerFactory.getLogger(MitreIdAttrsConfig.class);

	private String mitreClientIdAttr;
	private String clientIdAttr;
	private String redirectUrisAttr;
	private String clientNameAttr;
	private String clientDescriptionAttr;
	private String requiredScopesAttr;
	private String contactAttr;
	private String informationUrlAttr;
	private String grantTypesAttrs;
	private String responseTypesAttr;
	private String allowIntrospectionAttr;
	private String privacyPolicyUriAttr;

	public String getMitreClientIdAttr() {
		return mitreClientIdAttr;
	}

	public void setMitreClientIdAttr(String mitreClientIdAttr) {
		this.mitreClientIdAttr = mitreClientIdAttr;
	}

	public String getClientIdAttr() {
		return clientIdAttr;
	}

	public void setClientIdAttr(String clientIdAttr) {
		this.clientIdAttr = clientIdAttr;
	}

	public String getRedirectUrisAttr() {
		return redirectUrisAttr;
	}

	public void setRedirectUrisAttr(String redirectUrisAttr) {
		this.redirectUrisAttr = redirectUrisAttr;
	}

	public String getClientNameAttr() {
		return clientNameAttr;
	}

	public void setClientNameAttr(String clientNameAttr) {
		this.clientNameAttr = clientNameAttr;
	}

	public String getClientDescriptionAttr() {
		return clientDescriptionAttr;
	}

	public void setClientDescriptionAttr(String clientDescriptionAttr) {
		this.clientDescriptionAttr = clientDescriptionAttr;
	}

	public String getRequiredScopesAttr() {
		return requiredScopesAttr;
	}

	public void setRequiredScopesAttr(String requiredScopesAttr) {
		this.requiredScopesAttr = requiredScopesAttr;
	}

	public String getContactAttr() {
		return contactAttr;
	}

	public void setContactAttr(String contactAttr) {
		this.contactAttr = contactAttr;
	}

	public String getInformationUrlAttr() {
		return informationUrlAttr;
	}

	public void setInformationUrlAttr(String informationUrlAttr) {
		this.informationUrlAttr = informationUrlAttr;
	}

	public String getGrantTypesAttrs() {
		return grantTypesAttrs;
	}

	public void setGrantTypesAttrs(String grantTypesAttrs) {
		this.grantTypesAttrs = grantTypesAttrs;
	}

	public String getResponseTypesAttr() {
		return responseTypesAttr;
	}

	public void setResponseTypesAttr(String responseTypesAttr) {
		this.responseTypesAttr = responseTypesAttr;
	}

	public String getAllowIntrospectionAttr() {
		return allowIntrospectionAttr;
	}

	public void setAllowIntrospectionAttr(String allowIntrospectionAttr) {
		this.allowIntrospectionAttr = allowIntrospectionAttr;
	}

	public String getPrivacyPolicyUriAttr() {
		return privacyPolicyUriAttr;
	}

	public void setPrivacyPolicyUriAttr(String privacyPolicyUriAttr) {
		this.privacyPolicyUriAttr = privacyPolicyUriAttr;
	}

}
