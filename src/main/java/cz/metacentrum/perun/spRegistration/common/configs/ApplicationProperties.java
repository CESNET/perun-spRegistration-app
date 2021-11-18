package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import java.util.HashSet;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    @NotEmpty private Set<Long> adminIds;
    @NotBlank private String proxyIdentifier;
    @NotEmpty private Set<ServiceProtocol> protocolsEnabled;
    @NotEmpty private Set<String> languagesEnabled;
    @NotBlank private String secretKey;
    @NotBlank private String hostUrl;
    @NotBlank private String logoutUrl;
    @NotNull private AttributesProperties attributesProperties;
    @NotNull private ApprovalsProperties approvalsProperties;
    @NotNull private FrontendProperties frontendProperties;
    @NotNull private Long spManagersVoId;
    @NotNull private Long spManagersParentGroupId;
    @NotBlank private String mailsConfigFilePath;
    @NotNull private boolean startupSyncEnabled = false;
    private String devUserIdentifier;
    private boolean externalServicesEnabled = false;

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "adminIds=" + adminIds +
                ", proxyIdentifier='" + proxyIdentifier + '\'' +
                ", protocolsEnabled=" + protocolsEnabled +
                ", languagesEnabled=" + languagesEnabled +
                ", secretKey='*******************'" +
                ", hostUrl='" + hostUrl + '\'' +
                ", logoutUrl='" + logoutUrl + '\'' +
                ", mailsConfig='" + mailsConfigFilePath + '\'' +
                ", startupSyncEnabled='" + startupSyncEnabled + '\'' +
                ", devUserIdentifier=" + devUserIdentifier +
                ", externalServicesEnabled=" + externalServicesEnabled +
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
            ServiceProtocol p = ServiceProtocol.fromString(protocol);
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
