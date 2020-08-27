package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "jdbc")
public class JdbcProperties {

    private String driver;
    private String url;
    private String username;
    private String password;

    @PostConstruct
    public void postInit() {
        log.info("Initialized JDBC properties");
        log.debug("{}", this.toString());
    }

    @Override
    public String toString() {
        return "JdbcProperties{" +
                "driver='" + driver + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='*******************'" +
                '}';
    }

}
