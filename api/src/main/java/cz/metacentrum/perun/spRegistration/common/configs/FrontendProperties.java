package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {

    @NotNull private String footerHtml = "";
    @NotNull private String headerHtml = "";
    @NotBlank private String headerTitle = "SP Registration";
    @NotBlank private String headerLogoUrl = "https://perun.cesnet.cz/signpost/images/perun_3.png";

    @PostConstruct
    public void postInit() {
        log.info("Initialized Frontend properties");
        log.debug("{}", this.toString());
    }

}
