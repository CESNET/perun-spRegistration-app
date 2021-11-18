package cz.metacentrum.perun.spRegistration.persistence.enums;

import org.springframework.util.StringUtils;

public enum ServiceProtocol {

    SAML,
    OIDC;

    public static ServiceProtocol fromString(String protocol) {
        if (StringUtils.hasText(protocol)) {
            protocol = protocol.toUpperCase();
            switch (protocol) {
                case "OIDC":
                    return OIDC;
                case "SAML":
                    return SAML;
            }
        }
        return null;
    }

}
