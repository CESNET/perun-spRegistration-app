package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Setter
@Getter
@EqualsAndHashCode
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private Set<String> adminSubs;
    private Set<String> adminEntitlements = new HashSet<>();
    @NotBlank private String proxyIdentifier;
    @NotEmpty private Set<ServiceProtocol> protocolsEnabled;
    @NotEmpty private Set<String> languagesEnabled;
    @NotBlank private String secretKey;
    @NotBlank private String hostUrl;
    @NotBlank private String logoutUrl;
    @NotNull private Long spManagersVoId;
    @NotNull private Long spManagersParentGroupId;
    @NotBlank private String mailsConfigFilePath;
    @NotNull private boolean startupSyncEnabled = false;
    private boolean externalServicesEnabled = false;

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "adminSubs=" + adminSubs +
                ", adminEntitlements=" + adminEntitlements +
                ", proxyIdentifier='" + proxyIdentifier + '\'' +
                ", protocolsEnabled=" + protocolsEnabled +
                ", languagesEnabled=" + languagesEnabled +
                ", secretKey='*******************'" +
                ", hostUrl='" + hostUrl + '\'' +
                ", logoutUrl='" + logoutUrl + '\'' +
                ", mailsConfig='" + mailsConfigFilePath + '\'' +
                ", startupSyncEnabled='" + startupSyncEnabled + '\'' +
                ", externalServicesEnabled=" + externalServicesEnabled +
                '}';
    }

    @PostConstruct
    public void postInit() throws ConfigurationException {
        if ((adminSubs == null || adminSubs.isEmpty())
            && (adminEntitlements == null || adminEntitlements.isEmpty())
        ) {
            log.error("No admins have been configured via user identifiers nor entitlements. Check your configuration file.");
            throw new ConfigurationException("No admins have been configured");
        }
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

}
