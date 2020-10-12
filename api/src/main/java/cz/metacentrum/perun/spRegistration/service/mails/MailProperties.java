package cz.metacentrum.perun.spRegistration.service.mails;

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

    private String host = "localhost";
    private int port = 25;
    private String protocol = "smtp";
    private boolean auth = false;
    private String authUsername = null;
    private String authPassword = null;
    private boolean starttlsEnable = false;
    private int connectionTimeout = 3000;
    private int timeout = 5000;
    private int writeTimeout = 3000;
    private boolean debug = false;
    private String from = "spreg@spreg.aai.cesnet.cz";
    private String subjectPrefix = "SPREG:";
    private String footer = "Best regards,<br/>SPREG";
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
