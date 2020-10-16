package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
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
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "attributes")
public class AttributesProperties {

    @NonNull private Names names;
    @NonNull private Values values;

    @PostConstruct
    public void postInit() {
        log.info("Initialized attributes properties");
        log.debug("{}", this.toString());
    }

    public List<String> getAttrNames() {
        return Arrays.asList(
                this.names.getUserEmail(),
                this.names.getProxyIdentifier(),
                this.names.getMasterProxyIdentifier(),
                this.names.getIsTestSp(),
                this.names.getShowOnServiceList(),
                this.names.getAdministratorContact(),
                this.names.getOidcClientId(),
                this.names.getOidcClientSecret(),
                this.names.getEntityId(),
                this.names.getIsOidc(),
                this.names.getIsSaml(),
                this.names.getServiceName(),
                this.names.getServiceDesc(),
                this.names.getManagerGroup()
        );
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Names {
        // attr names
        @NotBlank private String userEmail = "urn:perun:user:attribute-def:def:preferredMail";
        @NotBlank private String proxyIdentifier = "urn:perun:facility:attribute-def:def:proxyIdentifiers";
        @NotBlank private String masterProxyIdentifier = "urn:perun:facility:attribute-def:def:masterProxyIdentifier";
        @NotBlank private String isTestSp = "urn:perun:facility:attribute-def:def:isTestSp";
        @NotBlank private String showOnServiceList = "urn:perun:facility:attribute-def:def:showOnServiceList";
        @NotBlank private String administratorContact = "urn:perun:facility:attribute-def:def:administrationContact";
        @NotBlank private String oidcClientId = "urn:perun:facility:attribute-def:def:OIDCClientID";
        @NotBlank private String oidcClientSecret = "urn:perun:facility:attribute-def:def:OIDCClientSecret";
        @NotBlank private String entityId = "urn:perun:facility:attribute-def:def:entityID";
        @NotBlank private String isOidc = "urn:perun:facility:attribute-def:def:isOidcFacility";
        @NotBlank private String isSaml = "urn:perun:facility:attribute-def:def:isSamlFacility";
        @NotBlank private String serviceName = "urn:perun:facility:attribute-def:def:serviceName";
        @NotBlank private String serviceDesc = "urn:perun:facility:attribute-def:def:serviceDescription";
        @NotBlank private String managerGroup = "urn:perun:facility:attribute-def:def:adminGroup";
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Values {
        @NotBlank private String proxyIdentifier = "https://login.cesnet.cz/idp/";
        @NotBlank private String masterProxyIdentifier = "https://login.cesnet.cz/idp/";
    }

}
