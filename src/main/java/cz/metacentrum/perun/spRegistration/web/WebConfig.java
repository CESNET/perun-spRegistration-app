package cz.metacentrum.perun.spRegistration.web;

import lombok.Setter;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Setter
public class WebConfig implements WebMvcConfigurer {

    @Value("${dev.enabled:false}")
    private boolean devEnabled;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (devEnabled) {
            registry.addMapping("/**").allowedOriginPatterns("*").allowCredentials(true);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector ajpConnector = new Connector("AJP/1.3");
        ajpConnector.setPort(8009);
        ajpConnector.setSecure(false);
        ajpConnector.setAllowTrace(false);
        ajpConnector.setScheme("http");
        ajpConnector.setProperty("packetSize", "65536");
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setSecretRequired(false);
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setAllowedRequestAttributesPattern(".*");
        ((AbstractAjpProtocol) ajpConnector.getProtocolHandler()).setTomcatAuthentication(false);
        tomcat.addAdditionalTomcatConnectors(ajpConnector);

        return tomcat;
    }

}
