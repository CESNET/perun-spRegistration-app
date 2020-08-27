package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationConfiguration {

    @NotEmpty private Set<Long> adminIds;
    @NotBlank private String userEmailAttrName;

}


