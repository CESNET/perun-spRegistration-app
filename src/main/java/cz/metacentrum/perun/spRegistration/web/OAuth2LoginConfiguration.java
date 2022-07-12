package cz.metacentrum.perun.spRegistration.web;

import static cz.metacentrum.perun.spRegistration.web.SecurityConfiguration.ROLE_ADMIN;
import static cz.metacentrum.perun.spRegistration.web.SecurityConfiguration.ROLE_USER;
import static org.springframework.security.oauth2.core.AuthenticationMethod.FORM;
import static org.springframework.security.oauth2.core.AuthenticationMethod.HEADER;
import static org.springframework.security.oauth2.core.AuthenticationMethod.QUERY;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.IMPLICIT;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@EnableWebSecurity
@Configuration
@Slf4j
public class OAuth2LoginConfiguration {

    private static final String AUTHORIZATION_ENDPOINT = "authorization_endpoint";
    private static final String TOKEN_ENDPOINT = "token_endpoint";
    private static final String USERINFO_ENDPOINT = "userinfo_endpoint";
    private static final String JWKS_URI = "jwks_uri";
    private final OidcProperties oidcProperties;
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public OAuth2LoginConfiguration(OidcProperties oidcProperties, ApplicationProperties applicationProperties)
    {
        this.oidcProperties = oidcProperties;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            mappedAuthorities.add(ROLE_USER);

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    Map<String, Object> claims = new HashMap<>();
                    if (oidcUserAuthority.getIdToken() != null) {
                        claims.putAll(oidcUserAuthority.getIdToken().getClaims());
                    }
                    if (oidcUserAuthority.getUserInfo() != null) {
                        claims.putAll(oidcUserAuthority.getUserInfo().getClaims());
                    }
                    mappedAuthorities.addAll(mapAuthoritiesFromAttributes(claims));
                } else if (authority instanceof OAuth2UserAuthority) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;
                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                    mappedAuthorities.addAll(mapAuthoritiesFromAttributes(userAttributes));
                }
            });

            return mappedAuthorities;
        };
    }

    private Set<SimpleGrantedAuthority> mapAuthoritiesFromAttributes(Map<String, Object> claims) {
        boolean isAdminBySub = false;
        boolean isAdminByEntitlement = false;
        String sub = (String) claims.getOrDefault(IdTokenClaimNames.SUB, null);

        if (applicationProperties.getAdminSubs() != null
            && !applicationProperties.getAdminSubs().isEmpty()
            && StringUtils.hasText(sub))
        {
            isAdminBySub = applicationProperties.getAdminSubs().contains(sub);
            if (isAdminBySub) {
                log.info("User '{}' mapped to admin because of listed identifier '{}'", sub, sub);
            }
        }
        if (applicationProperties.getAdminEntitlements() != null
            && !applicationProperties.getAdminEntitlements().isEmpty())
        {
            List<String> entitlements = (List<String>) claims.getOrDefault(
                "eduperson_entitlement", new ArrayList<>());
            if (entitlements != null && entitlements.size() > 0) {
                Set<String> entitlementsSet = new HashSet<>(entitlements);
                isAdminByEntitlement = !Collections.disjoint(
                    entitlementsSet, applicationProperties.getAdminEntitlements());
                if (isAdminByEntitlement) {
                    log.info("User '{}' mapped to admin because of entitlements '{}'", sub,
                        entitlementsSet.retainAll(applicationProperties.getAdminEntitlements()));
                }
            }

        }
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (isAdminBySub || isAdminByEntitlement) {
            authorities.add(ROLE_ADMIN);
        }
        return authorities;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        String authorizationEndpoint = oidcProperties.getAuthorizationEndpoint();
        String userinfoEndpoint = oidcProperties.getUserinfoEndpoint();
        String tokenEndpoint = oidcProperties.getTokenEndpoint();
        String jwksUri = oidcProperties.getJwkSetUri();
        if (oidcProperties.isFetchEndpointsFromWellKnown()) {
            String wellKnown = oidcProperties.getIssuer();
            if (!wellKnown.endsWith("/")) {
                wellKnown += '/';
            }
            wellKnown += ".well-known/openid-configuration";
            JsonNode response = restTemplate.getForObject(wellKnown, JsonNode.class);
            if (response == null || response instanceof NullNode) {
                log.error("Could not fetch OIDC metadata from well-known URL '{}'. " +
                    "Got response '{}'", wellKnown, response);
                throw new RuntimeException("Could not fetch data from well-known URL");
            }
            authorizationEndpoint = response.path(AUTHORIZATION_ENDPOINT).asText();
            userinfoEndpoint = response.path(USERINFO_ENDPOINT).asText();
            tokenEndpoint = response.path(TOKEN_ENDPOINT).asText();
            jwksUri = response.path(JWKS_URI).asText();
        }

        ClientAuthenticationMethod clientAuthenticationMethod = resolveAuthenticationMethod(
            oidcProperties.getAuthenticationMethod());
        AuthorizationGrantType authorizationGrantType = resolveAuthorizationGrantType(
            oidcProperties.getGrantType());
        AuthenticationMethod userinfoAuthenticationMethod = resolveUserInfoAuthenticationMethod(
            oidcProperties.getUserinfoEndpointAuthenticationMethod());

        return ClientRegistration.withRegistrationId(oidcProperties.getRegistrationId())
            .clientId(oidcProperties.getClientId())
            .clientSecret(oidcProperties.getClientSecret())
            .clientAuthenticationMethod(clientAuthenticationMethod)
            .authorizationGrantType(authorizationGrantType)
            .redirectUri(oidcProperties.getRedirectUri())
            .scope(oidcProperties.getScopes())
            .authorizationUri(authorizationEndpoint)
            .tokenUri(tokenEndpoint)
            .userInfoUri(userinfoEndpoint)
            .userInfoAuthenticationMethod(userinfoAuthenticationMethod)
            .userNameAttributeName(oidcProperties.getUsernameAttribute())
            .jwkSetUri(jwksUri)
            .clientName(oidcProperties.getClientName())
            .build();
    }

    private AuthenticationMethod resolveUserInfoAuthenticationMethod(String value) {
        Map<String, AuthenticationMethod> lookup = new HashMap<>();
        lookup.put(HEADER.getValue(), HEADER);
        lookup.put(FORM.getValue(), FORM);
        lookup.put(QUERY.getValue(), QUERY);
        if (value == null || !lookup.containsKey(value)) {
            log.warn("Authentication method '{}' has not been recognized. "
                    + "Allowed values are '{}'. Using '{}' as the default value.",
                value, lookup.keySet(), HEADER);
            return HEADER;
        } else {
            return lookup.get(value);
        }
    }

    private AuthorizationGrantType resolveAuthorizationGrantType(String value) {
        Map<String, AuthorizationGrantType> lookup = new HashMap<>();
        lookup.put(AUTHORIZATION_CODE.getValue(),
            AUTHORIZATION_CODE);
        lookup.put(IMPLICIT.getValue(), IMPLICIT);
        if (value == null || !lookup.containsKey(value)) {
            log.warn("Authorization grant type method '{}' has not been recognized. " +
                    "Allowed values are '{}'. Using '{}' as the default value.",
                value, lookup.keySet(), CLIENT_SECRET_BASIC);
            return AUTHORIZATION_CODE;
        } else {
            return lookup.get(value);
        }
    }

    private ClientAuthenticationMethod resolveAuthenticationMethod(String value) {
        Map<String, ClientAuthenticationMethod> lookup = new HashMap<>();
        lookup.put(NONE.getValue(), NONE);
        lookup.put(CLIENT_SECRET_BASIC.getValue(), CLIENT_SECRET_BASIC);
        lookup.put(CLIENT_SECRET_POST.getValue(), CLIENT_SECRET_POST);
        lookup.put(CLIENT_SECRET_JWT.getValue(), CLIENT_SECRET_JWT);
        lookup.put(PRIVATE_KEY_JWT.getValue(), PRIVATE_KEY_JWT);
        if (value == null || !lookup.containsKey(value)) {
            log.warn("Authentication method '{}' has not been recognized " +
                    "Allowed values are '{}'. Using '{}' as the default value.",
                value, lookup.keySet(), CLIENT_SECRET_BASIC);
            return CLIENT_SECRET_BASIC;
        } else {
            return lookup.get(value);
        }
    }

}
