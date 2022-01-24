package cz.metacentrum.perun.spRegistration.common.configs;

import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol.*;

import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import java.util.HashSet;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;
import org.springframework.util.StringUtils;

@Setter
@Getter
@EqualsAndHashCode
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    @NotEmpty private Set<Long> adminIds = new HashSet<>();
    @NotBlank private String proxyIdentifier;
    @NotEmpty private Set<ServiceProtocol> protocolsEnabled = Set.of(SAML, OIDC);
    @NotEmpty private Set<String> languagesEnabled = Set.of("en");
    @NotEmpty private Set<String> environmentsEnabled = Set.of("testing", "production");
    @NotBlank private String secretKey;
    @NotBlank private String hostUrl;
    @NotBlank private String logoutUrl;
    @NotNull private Long spManagersVoId;
    @NotNull private Long spManagersParentGroupId;
    @NotBlank private String mailsConfigFilePath;
    @NotNull private boolean startupSyncEnabled = false;
    private boolean externalServicesEnabled = false;

    private String devUserIdentifier;

    @NotNull private AttributesProperties attributesProperties;
    @NotNull private ApprovalsProperties approvalsProperties;
    @NotNull private FrontendProperties frontendProperties;

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
            "adminIds=" + adminIds +
            ", proxyIdentifier='" + proxyIdentifier + '\'' +
            ", protocolsEnabled=" + protocolsEnabled +
            ", languagesEnabled=" + languagesEnabled +
            ", environmentsEnabled=" + environmentsEnabled +
            ", secretKey='*******************'" +
            ", hostUrl='" + hostUrl + '\'' +
            ", logoutUrl='" + logoutUrl + '\'' +
            ", mailsConfig='" + mailsConfigFilePath + '\'' +
            ", startupSyncEnabled='" + startupSyncEnabled + '\'' +
            ", externalServicesEnabled=" + externalServicesEnabled +
            ", devUserIdentifier=" + devUserIdentifier +
            '}';
    }

    @PostConstruct
    public void postInit() {
        log.info("Initialized application properties");
        log.debug("{}", this);
    }

    public void setProtocolsEnabled(List<String> protocolsEnabled) {
        this.protocolsEnabled = new HashSet<>();
        if (protocolsEnabled == null || protocolsEnabled.size() < 1) {
            throw new IllegalArgumentException("No protocols enabled");
        }
        for (String protocol: protocolsEnabled) {
            if (!StringUtils.hasText(protocol)) {
                throw new IllegalArgumentException("No protocol parsed");
            }
            ServiceProtocol p = fromString(protocol);
            if (p == null) {
                throw new IllegalArgumentException("Unsupported protocol given");
            }
            this.protocolsEnabled.add(p);
        }
    }

    public boolean isAppAdmin(@NonNull Long id) {
        return adminIds.contains(id);
    }

}
