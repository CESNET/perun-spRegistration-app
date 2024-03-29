package cz.metacentrum.perun.spRegistration.common.configs;

import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {

    @NonNull private String footerHtml = "";
    @NonNull private String headerHtml = "";
    @NonNull private String headerTitle = "SP Registration";
    @NonNull private String headerLogoUrl = "assets/img/perun_3.svg";
    @NonNull private String faviconUrl = "assets/img/perun-ico.png";

    @PostConstruct
    public void postInit() {
        log.info("Initialized Frontend properties");
        log.debug("{}", this);
    }

    public void setHeaderLogoUrl(@NonNull String headerLogoUrl) {
        if (!StringUtils.hasText(headerLogoUrl)) {
            throw new IllegalArgumentException("HeaderLogoURL cannot be empty");
        }
    }

}
