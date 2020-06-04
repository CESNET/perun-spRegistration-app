package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class with application configuration
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class AppConfig {

	private Set<Long> appAdminIds;
	private String loginExtSource;
	private Map<String, PerunAttributeDefinition> perunAttributeDefinitionsMap = new HashMap<>();
	private Map<String, AttributeCategory> attributeCategoryMap = new HashMap<>();
	private String[] protocolsEnabled = new String[] {};
	private List<String> availableLanguages = new ArrayList<>();
	private String logoutUrl;

	private PerunConnector perunConnector;
	private String showOnServicesListAttributeName;
	private String isTestSpAttribute;
	private String entityIdAttribute;
	private String clientIdAttribute;
	private String clientSecretAttribute;
	private String masterProxyIdentifierAttribute;
	private String masterProxyIdentifierAttributeValue;
	private String proxyIdentifierAttribute;
	private String proxyIdentifierAttributeValue;
	private String adminsAttributeName;
	private String userEmailAttributeName;
	private String isSamlAttributeName;
	private String isOidcAttributeName;
	private String serviceNameAttributeName;
	private String serviceDescAttributeName;
	private Map<String, List<String>> prodTransferAuthoritiesMailsMap;

	private String footerHTML;
	private String headerLogo;
	private String headerTitle;
	private String headerHTML;

	private long confirmationPeriodDays;
	private long confirmationPeriodHours;
	private SecretKeySpec secret;
	private boolean specifyAuthoritiesEnabled;
	private String signaturesEndpointUrl;
	private String adminsEndpoint;
	private Long spAdminsRootVoId;
	private Long spAdminsRootGroupId;
	private String adminsGroupAttribute;

	public Set<Long> getAppAdminIds() {
		return Collections.unmodifiableSet(appAdminIds);
	}

	public void setAppAdminIds(Set<Long> appAdminIds) {
		this.appAdminIds = appAdminIds;
	}

	public List<String> getAvailableLanguages() {
		return availableLanguages;
	}

	public void setAvailableLanguages(List<String> availableLanguages) {
		this.availableLanguages = availableLanguages;
	}

	public PerunConnector getPerunConnector() {
		return perunConnector;
	}

	public void setPerunConnector(PerunConnector perunConnector) {
		this.perunConnector = perunConnector;
	}

	public String getLoginExtSource() {
		return loginExtSource;
	}

	public void setLoginExtSource(String loginExtSource) {
		this.loginExtSource = loginExtSource;
	}

	public String getFooterHTML() {
		return footerHTML;
	}

	public void setFooterHTML(String footerHTML) {
		this.footerHTML = footerHTML;
	}

	public String getHeaderLogo() {
		return headerLogo;
	}

	public void setHeaderLogo(String headerLogo) {
		this.headerLogo = headerLogo;
	}

	public String getHeaderTitle() {
		return headerTitle;
	}

	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}

	public String getHeaderHTML() {
		return headerHTML;
	}

	public void setHeaderHTML(String headerHTML) {
		this.headerHTML = headerHTML;
	}

	public long getConfirmationPeriodDays() {
		return confirmationPeriodDays;
	}

	public String[] getProtocolsEnabled() {
		return protocolsEnabled;
	}

	public void setProtocolsEnabled(String[] protocolsEnabled) {
		if (protocolsEnabled != null && protocolsEnabled.length > 0) {
			String[] transf = new String[protocolsEnabled.length];
			for (int i = 0; i < protocolsEnabled.length; i++) {
				transf[i] = protocolsEnabled[i].toLowerCase();
			}
			this.protocolsEnabled = transf;
		}
	}

	public void setConfirmationPeriodDays(long confirmationPeriodDays) {
		this.confirmationPeriodDays = confirmationPeriodDays;
	}

	public long getConfirmationPeriodHours() {
		return confirmationPeriodHours;
	}

	public void setConfirmationPeriodHours(long confirmationPeriodHours) {
		this.confirmationPeriodHours = confirmationPeriodHours;
	}

	public SecretKeySpec getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		secret = fixSecret(secret);
		MessageDigest sha;
		try {
			byte[] key = secret.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			this.secret = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public boolean getSpecifyAuthoritiesEnabled() {
		return specifyAuthoritiesEnabled;
	}

	public void setSpecifyAuthoritiesEnabled(boolean specifyAuthoritiesEnabled) {
		this.specifyAuthoritiesEnabled = specifyAuthoritiesEnabled;
	}

	public Map<String, PerunAttributeDefinition> getPerunAttributeDefinitionsMap() {
		return perunAttributeDefinitionsMap;
	}

	public void setPerunAttributeDefinitionsMap(Map<String, PerunAttributeDefinition> perunAttributeDefinitionsMap) {
		this.perunAttributeDefinitionsMap = perunAttributeDefinitionsMap;
	}

	public Map<String, List<String>> getProdTransferAuthoritiesMailsMap() { return prodTransferAuthoritiesMailsMap; }

	public void setProdTransferAuthoritiesMailsMap(Properties prodTransferAuthoritiesMailsMap) {
		this.prodTransferAuthoritiesMailsMap = new HashMap<>();

		for (Map.Entry<Object, Object> entry : prodTransferAuthoritiesMailsMap.entrySet()) {
			List<String> emails = Arrays.asList(((String)entry.getValue()).split(","));
			String displayedText = (String) entry.getKey();
			this.prodTransferAuthoritiesMailsMap.put(displayedText, emails);
		}
	}

	public String getSignaturesEndpointUrl() {
		return signaturesEndpointUrl;
	}

	public void setSignaturesEndpointUrl(String signaturesEndpointUrl) {
		this.signaturesEndpointUrl = signaturesEndpointUrl;
	}

	public String getAdminsEndpoint() {
		return adminsEndpoint;
	}

	public void setAdminsEndpoint(String adminsEndpoint) {
		this.adminsEndpoint = adminsEndpoint;
	}

	public Map<String, AttributeCategory> getAttributeCategoryMap() {
		return attributeCategoryMap;
	}

	public void setAttributeCategoryMap(Map<String, AttributeCategory> attributeCategoryMap) {
		this.attributeCategoryMap = attributeCategoryMap;
	}

	public Long getSpAdminsRootVoId() {
		return spAdminsRootVoId;
	}

	public void setSpAdminsRootVoId(Long spAdminsRootVoId) {
		this.spAdminsRootVoId = spAdminsRootVoId;
	}

	public Long getSpAdminsRootGroupId() {
		return spAdminsRootGroupId;
	}

	public void setSpAdminsRootGroupId(Long spAdminsRootGroupId) {
		this.spAdminsRootGroupId = spAdminsRootGroupId;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	// ATTRIBUTES

	public String getMasterProxyIdentifierAttribute() {
		return masterProxyIdentifierAttribute;
	}

	public void setMasterProxyIdentifierAttribute(String masterProxyIdentifierAttribute) throws ConnectorException {
		this.masterProxyIdentifierAttribute = masterProxyIdentifierAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(masterProxyIdentifierAttribute);
		perunAttributeDefinitionsMap.put(masterProxyIdentifierAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.SERVICE);
	}

	public String getProxyIdentifierAttribute() {
		return proxyIdentifierAttribute;
	}

	public void setProxyIdentifierAttribute(String proxyIdentifierAttribute) throws ConnectorException {
		this.proxyIdentifierAttribute = proxyIdentifierAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(proxyIdentifierAttribute);
		perunAttributeDefinitionsMap.put(proxyIdentifierAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.SERVICE);
	}

	public String getIsTestSpAttribute() {
		return isTestSpAttribute;
	}

	public void setIsTestSpAttribute(String isTestSpAttribute) throws ConnectorException {
		this.isTestSpAttribute = isTestSpAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(isTestSpAttribute);
		perunAttributeDefinitionsMap.put(isTestSpAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.SERVICE);
	}

	public String getShowOnServicesListAttributeName() {
		return showOnServicesListAttributeName;
	}

	public void setShowOnServicesListAttributeName(String showOnServicesListAttributeName) throws ConnectorException {
		this.showOnServicesListAttributeName = showOnServicesListAttributeName;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(showOnServicesListAttributeName);
		perunAttributeDefinitionsMap.put(showOnServicesListAttributeName, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.SERVICE);
	}

	public String getAdminsAttributeName() {
		return adminsAttributeName;
	}

	public void setAdminsAttributeName(String adminsAttributeName) {
		this.adminsAttributeName = adminsAttributeName;
	}

	public String getUserEmailAttributeName() {
		return userEmailAttributeName;
	}

	public void setUserEmailAttributeName(String userEmailAttributeName) {
		this.userEmailAttributeName = userEmailAttributeName;
	}

	public String getEntityIdAttribute() {
		return entityIdAttribute;
	}

	public void setEntityIdAttribute(String entityIdAttribute) {
		this.entityIdAttribute = entityIdAttribute;
	}

	public String getClientIdAttribute() {
		return clientIdAttribute;
	}

	public void setClientIdAttribute(String clientIdAttribute) throws ConnectorException {
		this.clientIdAttribute = clientIdAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(clientIdAttribute);
		perunAttributeDefinitionsMap.put(clientIdAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.PROTOCOL);
	}

	public String getClientSecretAttribute() {
		return clientSecretAttribute;
	}

	public void setClientSecretAttribute(String clientSecretAttribute) throws ConnectorException {
		this.clientSecretAttribute = clientSecretAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(clientSecretAttribute);
		perunAttributeDefinitionsMap.put(clientSecretAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.PROTOCOL);
	}

	public String getIsSamlAttributeName() {
		return isSamlAttributeName;
	}

	public void setIsSamlAttributeName(String isSamlAttributeName) throws ConnectorException {
		this.isSamlAttributeName = isSamlAttributeName;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(isSamlAttributeName);
		perunAttributeDefinitionsMap.put(isSamlAttributeName, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.PROTOCOL);
	}

	public String getIsOidcAttributeName() {
		return isOidcAttributeName;
	}

	public void setIsOidcAttributeName(String isOidcAttributeName) throws ConnectorException {
		this.isOidcAttributeName = isOidcAttributeName;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(isOidcAttributeName);
		perunAttributeDefinitionsMap.put(isOidcAttributeName, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.PROTOCOL);
	}

	public String getMasterProxyIdentifierAttributeValue() {
		return masterProxyIdentifierAttributeValue;
	}

	public void setMasterProxyIdentifierAttributeValue(String masterProxyIdentifierAttributeValue) {
		this.masterProxyIdentifierAttributeValue = masterProxyIdentifierAttributeValue;
	}

	public String getProxyIdentifierAttributeValue() {
		return proxyIdentifierAttributeValue;
	}

	public void setProxyIdentifierAttributeValue(String proxyIdentifierAttributeValue) {
		this.proxyIdentifierAttributeValue = proxyIdentifierAttributeValue;
	}

	public String getServiceNameAttributeName() {
		return serviceNameAttributeName;
	}

	public void setServiceNameAttributeName(String serviceNameAttributeName) {
		this.serviceNameAttributeName = serviceNameAttributeName;
	}

	public String getServiceDescAttributeName() {
		return serviceDescAttributeName;
	}

	public void setServiceDescAttributeName(String serviceDescAttributeName) {
		this.serviceDescAttributeName = serviceDescAttributeName;
	}

	public String getAdminsGroupAttribute() {
		return adminsGroupAttribute;
	}

	public void setAdminsGroupAttribute(String adminsGroupAttribute) throws ConnectorException {
		this.adminsGroupAttribute = adminsGroupAttribute;

		PerunAttributeDefinition def = perunConnector.getAttributeDefinition(adminsGroupAttribute);
		perunAttributeDefinitionsMap.put(adminsGroupAttribute, def);
		attributeCategoryMap.put(def.getFullName(), AttributeCategory.SERVICE);
	}

	@Override
	public String toString() {
		return	"appAdminIds: " + appAdminIds + "'\n" +
				"loginExtSource: '" + loginExtSource + "'\n" +
				"perunAttributeDefinitionsMap: " + perunAttributeDefinitionsMap + "'\n" +
				"protocolsEnabled=" + Arrays.toString(protocolsEnabled) + "'\n" +
				"availableLanguages=" + availableLanguages + "'\n" +
				"perunConnector=" + perunConnector + "'\n" +
				"footerHTML: '" + footerHTML + "'\n" +
				"headerLogo: '" + headerLogo + "'\n" +
				"headerTitle: '" + headerTitle + "'\n" +
				"headerHTML: '" + headerHTML + "'\n" +
				"confirmationPeriodDays: " + confirmationPeriodDays + "'\n" +
				"confirmationPeriodHours: " + confirmationPeriodHours + "'\n" +
				"secret: " + secret + "'\n" +
				"specifyAuthoritiesEnabled: " + specifyAuthoritiesEnabled + "'\n" +
				"signaturesEndpointUrl: '" + signaturesEndpointUrl + "'\n" +
				"adminsEndpoint: '" + adminsEndpoint + "'\n" +
				"proxyIdentifierAttribute: '" + proxyIdentifierAttribute + "'\n" +
				"masterProxyIdentifierAttribute: '" + masterProxyIdentifierAttribute + "'\n" +
				"proxyIdentifierAttributeValue: '" + proxyIdentifierAttributeValue + "'\n" +
				"adminsAttributeName: '" + adminsAttributeName + "'\n" +
				"userEmailAttributeName: '" + userEmailAttributeName + "'\n" +
				"showOnServicesListAttributeName: '" + showOnServicesListAttributeName + "'\n" +
				"isTestSpAttribute: '" + isTestSpAttribute + "'\n";
	}

	// custom methods

	public PerunAttributeDefinition getAttrDefinition(String fullName) {
		return perunAttributeDefinitionsMap.get(fullName);
	}

	public boolean isAppAdmin(Long userId) {
		return appAdminIds.contains(userId);
	}

	private String fixSecret(String s) {
		if (s.length() < 32) {
			int missingLength = 32 - s.length();
			StringBuilder sBuilder = new StringBuilder(s);
			for (int i = 0; i < missingLength; i++) {
				sBuilder.append('A');
			}
			s = sBuilder.toString();
		}
		return s.substring(0, 32);
	}

	public Map<AttributeCategory, Map<String, PerunAttribute>> filterInvalidAttributes(Map<AttributeCategory, Map<String, PerunAttribute>> attributes) {
		if (attributes == null) {
			return null;
		} else if (attributes.isEmpty()) {
			return attributes;
		}

		Map<AttributeCategory, Map<String, PerunAttribute>> valid = new HashMap<>();
		for (Map.Entry<AttributeCategory, Map<String, PerunAttribute>> categoryMapEntry : attributes.entrySet()) {
			AttributeCategory category = categoryMapEntry.getKey();
			Map<String, PerunAttribute> attributeMap = categoryMapEntry.getValue();
			Map<String, PerunAttribute> validInCategory = new HashMap<>();
			for (String attrName : attributeMap.keySet()) {
				if (perunAttributeDefinitionsMap.containsKey(attrName)) {
					validInCategory.put(attrName, attributeMap.get(attrName));
				}
			}
			valid.put(category, validInCategory);
		}

		return valid;

	}

	public AttributeCategory getAttrCategory(String fullName) {
		if (fullName == null) {
			throw new IllegalArgumentException("Full name cannot be null");
		}

		return attributeCategoryMap.get(fullName);
	}
}
