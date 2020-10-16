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
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "approvals")
public class ApprovalsProperties {

    @NonNull private ConfirmationPeriod confirmationPeriod;
    @NotBlank private String adminsEndpoint = "https://dev.spreg.aai.cesnet.cz/spreg/auth/facilities/addAdmin/sign";
    @NotBlank private String authoritiesEndpoint = "https://dev.spreg.aai.cesnet.cz/spreg/auth/sign";
    @NonNull private TransferAuthorities transferAuthorities;

    @PostConstruct
    public void postInit() {
        log.info("Initialized approvals properties");
        log.debug("{}", this.toString());
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferAuthorities {
        private boolean allowInput = true;
        @NotEmpty private List<String> defaultEntries;
        private final Map<String, List<String>> selectionEntries = new HashMap<>();

        public void setSelectionEntries(List<TransferAuthoritiesMapEntry> entries) {
            if (entries == null || entries.isEmpty()) {
                log.warn("No selection entries provided");
                return;
            }
            for (TransferAuthoritiesMapEntry entry: entries) {
                selectionEntries.put(entry.getDisplayValue(), entry.getEmails());
            }
        }
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferAuthoritiesMapEntry {
        @NotBlank private String displayValue;
        @NotEmpty private List<String> emails;
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmationPeriod {
        private int days = 30;
        private int hours = 0;
    }

}
