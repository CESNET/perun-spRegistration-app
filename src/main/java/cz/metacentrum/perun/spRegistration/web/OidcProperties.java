package cz.metacentrum.perun.spRegistration.web;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "oidc")
@Component
public class OidcProperties {

    private boolean fetchEndpointsFromWellKnown = true;
    private String issuer;
    private String registrationId;
    private String clientId;
    private String clientSecret;
    private String authenticationMethod = "client_secret_basic";
    private String grantType = "authorization_code";
    private String redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
    private List<String> scopes;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userinfoEndpoint;
    private String userinfoEndpointAuthenticationMethod;
    private String usernameAttribute = "sub";
    private String jwkSetUri;
    private String clientName = "aai";

}
