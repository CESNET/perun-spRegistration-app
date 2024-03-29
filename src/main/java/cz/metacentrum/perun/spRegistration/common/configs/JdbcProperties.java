package cz.metacentrum.perun.spRegistration.common.configs;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Component
@Configuration
@ConfigurationProperties(prefix = "jdbc")
public class JdbcProperties {

    @NotBlank private String driver;
    @NotBlank private String url;
    @NotBlank private String username;
    @NotBlank private String password;
    private int maxPoolSize = 40;

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
