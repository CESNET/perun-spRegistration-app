package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {

    @NonNull private String footerHtml = "";
    @NonNull private String headerHtml = "";
    @NonNull private String headerTitle = "SP Registration";
    @NonNull private String headerLogoUrl = "https://perun.cesnet.cz/signpost/images/perun_3.png";

    @PostConstruct
    public void postInit() {
        log.info("Initialized Frontend properties");
        log.debug("{}", this.toString());
    }

    public void setHeaderLogoUrl(String headerLogoUrl) {
        if (!StringUtils.hasText(headerLogoUrl)) {
            throw new IllegalArgumentException("HeaderLogoURL cannot be empty");
        }
    }

}
