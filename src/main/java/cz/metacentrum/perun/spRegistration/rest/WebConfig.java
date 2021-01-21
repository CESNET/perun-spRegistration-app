package cz.metacentrum.perun.spRegistration.rest;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.rest.interceptors.UserSettingInterceptor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Setter
public class WebConfig implements WebMvcConfigurer {

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApplicationProperties applicationProperties;

    @Value("${dev.enabled:false}")
    private boolean devEnabled;

    @Autowired
    public WebConfig(@NonNull PerunAdapter perunAdapter,
                     @NonNull AttributesProperties attributesProperties,
                     @NonNull ApplicationProperties applicationProperties)
    {
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    @Autowired
    public UserSettingInterceptor userSettingInterceptor(@NonNull PerunAdapter perunAdapter,
                                                         @NonNull AttributesProperties attributesProperties,
                                                         @NonNull ApplicationProperties applicationProperties)
    {
        return new UserSettingInterceptor(perunAdapter, attributesProperties, applicationProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (devEnabled) {
            String path = "/**";
            registry.addInterceptor(userSettingInterceptor(perunAdapter, attributesProperties, applicationProperties))
                    .addPathPatterns(path)
                    .excludePathPatterns("/api/config/**", "/", "");
        } else {
            String path = "/auth/**";

            registry.addInterceptor(userSettingInterceptor(perunAdapter, attributesProperties, applicationProperties))
                    .addPathPatterns(path)
                    .excludePathPatterns("/api/config/**");
        }
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector ajpConnector = new Connector("AJP/1.3");
        ajpConnector.setPort(8009);
        ajpConnector.setSecure(false);
        ajpConnector.setAllowTrace(false);
        ajpConnector.setScheme("http");
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setSecretRequired(false);
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setAllowedRequestAttributesPattern(".*");
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setTomcatAuthentication(false);
        tomcat.addAdditionalTomcatConnectors(ajpConnector);

        return tomcat;
    }

}
