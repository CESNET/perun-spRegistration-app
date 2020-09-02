package cz.metacentrum.perun.spRegistration.common.configs;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
@Component
@ConfigurationProperties(prefix = "approvals")
public class ApprovalsProperties {

    private int confirmationPeriodDays = 30;
    private int confirmationPeriodHours = 0;
    private boolean specifyOwn = true;
    @NotBlank private String adminsEndpoint = "https://dev.spreg.aai.cesnet.cz/spreg/auth/facilities/addAdmin/sign";
    @NotBlank private String authoritiesEndpoint = "https://dev.spreg.aai.cesnet.cz/spreg/auth/sign";
    private final Map<String, List<String>> transferAuthoritiesMap = new HashMap<>();
    @NotEmpty private List<String> defaultAuthorities;

    @PostConstruct
    public void postInit() {
        log.info("Initialized approvals properties");
        log.debug("{}", this.toString());
    }

    public void setTransferAuthoritiesMap(@NonNull List<TransferAuthoritiesMapEntry> entries) {
       for (TransferAuthoritiesMapEntry entry: entries) {
           transferAuthoritiesMap.put(entry.getDisplayValue(), entry.getEmails());
       }
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TransferAuthoritiesMapEntry {
        @NotBlank private String displayValue;
        @NotEmpty private List<String> emails;
    }

}
