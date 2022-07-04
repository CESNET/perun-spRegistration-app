package cz.metacentrum.perun.spRegistration.web;

import static cz.metacentrum.perun.spRegistration.web.controllers.AuthController.SESS_ATTR_USER;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Slf4j
public class AuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final PerunAdapter adapter;
    private final AppBeansContainer appBeansContainer;

    public AuthSuccessHandler(PerunAdapter adapter, AppBeansContainer appBeansContainer) {
        super();
        this.adapter = adapter;
        this.appBeansContainer = appBeansContainer;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
        throws ServletException, IOException
    {
        if (authentication != null && authentication.getAuthorities() != null) {
            log.debug("User '{}' has logged in", authentication.getName());

            DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
            String sub = userDetails.getSubject();
            boolean isAdmin = authentication.getAuthorities() != null
                && authentication.getAuthorities().contains(SecurityConfiguration.ROLE_ADMIN);
            try {
                User u = adapter.getUserWithEmail(sub, appBeansContainer.getApplicationProperties().getProxyIdentifier(),
                    appBeansContainer.getAttributesProperties().getNames().getUserEmail());
                if (u != null) {
                    u.setAppAdmin(isAdmin);
                }
                HttpSession sess = request.getSession();
                if (sess == null) {
                    log.error("No session available, cannot store Perun USER object");
                } else {
                    sess.setAttribute(SESS_ATTR_USER, u);
                }
            } catch (PerunUnknownException | PerunConnectionException e) {
                log.warn("Could not fetch user from Perun");
                log.debug("Details: ", e);
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
