package cz.metacentrum.perun.spRegistration.service.mails;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Component
@Slf4j
public class MailBeans {

    @Bean
    @Autowired
    public JavaMailSender javaMailSender(@NonNull MailProperties mailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        if (mailProperties.getAuthUsername() != null) {
            mailSender.setUsername(mailProperties.getAuthUsername());
        }

        if (mailProperties.getAuthPassword() != null) {
            mailSender.setPassword(mailProperties.getAuthPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", mailProperties.getProtocol());
        props.put("mail.smtp.auth", mailProperties.isAuth());
        props.put("mail.smtp.starttls.enable", mailProperties.isStarttlsEnable());
        props.put("mail.smtp.connectiontimeout", mailProperties.getConnectionTimeout());
        props.put("mail.smtp.timeout", mailProperties.getTimeout());
        props.put("mail.smtp.writetimeout", mailProperties.getWriteTimeout());
        props.put("mail.debug", mailProperties.isDebug());

        return mailSender;
    }

    @Bean
    @Autowired
    public MailProperties mailProperties(@NonNull ApplicationProperties applicationProperties) {
        String path = applicationProperties.getMailsConfig();
        try {
            return this.getMailPropertiesFromYaml(path);
        } catch (IOException e) {
            log.error("Error when reading properties file ({}) for mails!", path, e);
            throw new IllegalArgumentException("Could not load config for mails");
        }
    }

    private MailProperties getMailPropertiesFromYaml(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), new TypeReference<MailProperties>() {});
    }

}
