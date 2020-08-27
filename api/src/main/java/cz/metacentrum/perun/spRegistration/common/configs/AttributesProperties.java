package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "attributes")
public class AttributesProperties {

    // attr names
    @NotBlank private String userEmailAttrName = "urn:perun:user:attribute-def:def:preferredMail";
    @NotBlank private String proxyIdentifierAttrName = "urn:perun:facility:attribute-def:def:proxyIdentifiers";
    @NotBlank private String masterProxyIdentifierAttrName = "urn:perun:facility:attribute-def:def:masterProxyIdentifier";
    @NotBlank private String isTestSpAttrName = "urn:perun:facility:attribute-def:def:isTestSp";
    @NotBlank private String showOnServiceListAttrName = "urn:perun:facility:attribute-def:def:showOnServiceList";
    @NotBlank private String administratorContactAttrName = "urn:perun:facility:attribute-def:def:administrationContact";
    @NotBlank private String oidcClientIdAttrName = "urn:perun:facility:attribute-def:def:OIDCClientID";
    @NotBlank private String oidcClientSecretAttrName = "urn:perun:facility:attribute-def:def:OIDCClientSecret";
    @NotBlank private String entityIdAttrName = "urn:perun:facility:attribute-def:def:entityID";
    @NotBlank private String isOidcAttrName = "urn:perun:facility:attribute-def:def:isOidcFacility";
    @NotBlank private String isSamlAttrName = "urn:perun:facility:attribute-def:def:isSamlFacility";
    @NotBlank private String serviceNameAttrName = "urn:perun:facility:attribute-def:def:serviceName";
    @NotBlank private String serviceDescAttrName = "urn:perun:facility:attribute-def:def:serviceDescription";
    @NotBlank private String managerGroupAttrName = "urn:perun:facility:attribute-def:def:adminGroup";

    // attr values
    @NotBlank private String proxyIdentifierAttrValue = "https://login.cesnet.cz/idp/";
    @NotBlank private String masterProxyIdentifierAttrValue= "https://login.cesnet.cz/idp/";

    // additional paths
    @NotBlank private String serviceAttributesConfig = "/etc/perun-spreg/attrs/service.yml";
    @NotBlank private String organizationAttrsConfig = "/etc/perun-spreg/attrs/org.yml";
    private String samlAttrsConfig = "/etc/perun-spreg/attrs/saml.yml";
    private String oidcAttrsConfig = "/etc/perun-spreg/attrs/oidc.yml";
    @NotBlank private String acAttrsConfig = "/etc/perun-spreg/attrs/access_control.yml";

    @PostConstruct
    public void postInit() {
        log.info("Initialized attributes properties");
        log.debug("{}", this.toString());
    }

    public List<String> getAttrNames() {
        return Arrays.asList(
                userEmailAttrName,
                proxyIdentifierAttrName,
                masterProxyIdentifierAttrName,
                isTestSpAttrName,
                showOnServiceListAttrName,
                administratorContactAttrName,
                oidcClientIdAttrName,
                oidcClientSecretAttrName,
                entityIdAttrName,
                isOidcAttrName,
                isSamlAttrName,
                serviceNameAttrName,
                serviceDescAttrName,
                managerGroupAttrName
        );
    }

}
