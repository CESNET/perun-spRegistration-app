package cz.metacentrum.perun.spRegistration.service.mails;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class MailTemplate {

    private final Map<String, String> subject = new HashMap<>();
    private final Map<String, String> message = new HashMap<>();

    public MailTemplate(Map<String, String> subject, Map<String, String> message) {
        this.setSubject(subject);
        this.setMessage(message);
    }

    public void setSubject(Map<String, String> subject) {
        this.subject.clear();
        if (subject != null) {
            this.subject.putAll(subject);
        }
    }

    public void setMessage(Map<String, String> message) {
        this.message.clear();
        if (message != null) {
            this.message.putAll(message);
        }
    }

    public String getSubjectInLang(String lang) {
        return subject.getOrDefault(lang, "");
    }

    public String getMessageInLang(String lang) {
        return message.getOrDefault(lang, "");
    }

}
