package cz.metacentrum.perun.spRegistration.common.configs;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "inputs.config.paths")
public class InputConfigFilePathsProperties {

    @NotBlank private String service = "/etc/perun-spreg/attrs/service.yml";
    @NotBlank private String organization = "/etc/perun-spreg/attrs/org.yml";
    private String saml = "/etc/perun-spreg/attrs/saml.yml";
    private String oidc = "/etc/perun-spreg/attrs/oidc.yml";
    @NotBlank private String accessControl = "/etc/perun-spreg/attrs/access_control.yml";

}
