package cz.metacentrum.perun.spRegistration.service.mails;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class MailProperties {

    private String protocol = "smtp";
    private boolean auth = false;
    private boolean starttlsEnable = false;
    private int connectiontimeout = 3000;
    private int timeout = 5000;
    private int writetimeout = 3000;
    private boolean debug = false;
    private String host = "localhost";
    private int port = 25;
    private String username = null;
    private String password = null;
    private List<String> appAdminEmails;
    //private Map<String
            // TODO: finish this, come up with the config for the messages

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        if (username != null) {
            mailSender.setUsername(username);
        }

        if (password != null) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.connectiontimeout", connectiontimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writetimeout);
        props.put("mail.debug", debug);

        return mailSender;
    }
}
