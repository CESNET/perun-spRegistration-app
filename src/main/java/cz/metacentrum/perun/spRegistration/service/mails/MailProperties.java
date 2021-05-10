package cz.metacentrum.perun.spRegistration.service.mails;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailProperties {

    public static final String NULL = "@null";

    private boolean enabled = true;

    private String host = "localhost";

    private int port = 25;

    private String protocol = "smtp";

    private boolean auth = false;

    @JsonAlias({"authUsername", "auth_username"})
    private String authUsername = null;

    @JsonAlias({"authPassword", "auth_password"})
    private String authPassword = null;

    @JsonAlias({"starttlsEnable", "starttls_enable"})
    private boolean starttlsEnable = false;

    @JsonAlias({"connectionTimeout", "connection_timeout"})
    private int connectionTimeout = 3000;

    private int timeout = 5000;

    @JsonAlias({"writeTimeout", "write_timeout"})
    private int writeTimeout = 3000;

    private boolean debug = false;

    private String from = "spreg@spreg.aai.cesnet.cz";

    @JsonAlias({"subjectPrefix", "subject_prefix"})
    private String subjectPrefix = "SPREG:";

    private String footer = "Best regards,<br/>SPREG";

    @JsonAlias({"certificatePath", "certificate_path"})
    private String certificatePath = null;

    @JsonAlias({"privateKeyPath", "private_key_path"})
    private String privateKeyPath = null;

    @JsonAlias({"privateKeyAlgorithm", "private_key_algorithm"})
    private String privateKeyAlgorithm = "RSA";

    @JsonAlias({"signatureProviderAlgorithm", "signature_provider_algorithm"})
    private String signatureProviderAlgorithm = "SHA512withRSA";

    @JsonAlias({"appAdminEmails", "app_admin_emails"})
    private Set<String> appAdminEmails = new HashSet<>();

    private Map<String, MailTemplate> templates = new HashMap<>();

    public void setAuthUsername(String authUsername) {
        if (NULL.equalsIgnoreCase(authUsername)) {
            this.authUsername = null;
        } else {
            this.authUsername = authUsername;
        }
    }

    public void setAuthPassword(String authPassword) {
        if (NULL.equalsIgnoreCase(authPassword)) {
            this.authPassword = null;
        } else {
            this.authPassword = authPassword;
        }
    }

}
