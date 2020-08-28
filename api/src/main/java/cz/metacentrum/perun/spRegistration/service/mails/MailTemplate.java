package cz.metacentrum.perun.spRegistration.service.mails;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplate {

    private Map<String, String> subject;
    private Map<String, String> message;

    public String getSubjectInLang(String lang) {
        return subject.get(lang);
    }

    public String getMessageInLang(String lang) {
        return message.get(lang);
    }

    public String getInLang(String type, String lang) {
        if (type.equalsIgnoreCase("subject")) {
            return subject.get(lang);
        } else if (type.equalsIgnoreCase("message")) {
            return message.get(lang);
        }

        return null;
    }
}
