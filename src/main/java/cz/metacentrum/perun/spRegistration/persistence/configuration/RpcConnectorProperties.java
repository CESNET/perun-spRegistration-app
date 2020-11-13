package cz.metacentrum.perun.spRegistration.persistence.configuration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "connector.rpc")
public class RpcConnectorProperties {

    @NotBlank private String perunUrl = "https://perun-dev.cesnet.cz/ba/rpc";
    @NonNull private String perunUser;
    @NonNull private String perunPassword;
    private int requestTimeout = 30000;
    private int connectTimeout = 30000;
    private int socketTimeout = 60000;
    private int maxConnections = 20;
    private int maxConnectionsPerRoute = 18;

    @PostConstruct
    public void postInit() {
        log.info("Initialized RPC Connector properties");
        log.debug("{}", this.toString());
    }

    public void setPerunUrl(@NonNull String perunUrl) {
        if (perunUrl.endsWith("/")) {
            perunUrl = perunUrl.substring(0, perunUrl.length() - 1);
        }

        this.perunUrl = perunUrl;
    }

    @Override
    public String toString() {
        return "RpcConnectorProperties{" +
                "perunUrl='" + perunUrl + '\'' +
                ", perunUser='" + perunUser + '\'' +
                ", perunPassword='*******************'" +
                ", requestTimeout=" + requestTimeout +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                ", maxConnections=" + maxConnections +
                ", maxConnectionsPerRoute=" + maxConnectionsPerRoute +
                '}';
    }

}
