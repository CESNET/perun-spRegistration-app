package cz.metacentrum.perun.spRegistration.rest.interceptors;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@Slf4j
public class UserSettingInterceptor implements HandlerInterceptor {

	private final PerunAdapter connector;
	private final AttributesProperties attributesProperties;
	private final ApplicationProperties applicationProperties;

	@Value("${dev.enabled}")
	private boolean devEnabled;

	@Autowired
	public UserSettingInterceptor(PerunAdapter connector,
								  AttributesProperties attributesProperties,
								  ApplicationProperties applicationProperties)
	{
		this.connector = connector;
		this.attributesProperties = attributesProperties;
		this.applicationProperties = applicationProperties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.debug("UserSettingInterceptor 'preHandle()'");
		User userFromRequest = (User) request.getSession().getAttribute("user");


		if (userFromRequest == null && setUser(request) == null) {
			String url = request.getRequestURL().toString();
			int index = url.indexOf("/spreg/");
			url = url.substring(0, index);
			response.sendRedirect(url + "/spreg/");
			return false;
		}

		return true;
	}

	private User setUser(HttpServletRequest request) throws PerunUnknownException, PerunConnectionException {
		String userEmailAttr = attributesProperties.getAdministratorContactAttrName();
		String extSourceProxy = applicationProperties.getProxyIdentifier();
		log.info("settingUser");
		String sub;

		if (devEnabled) {
			sub = request.getHeader("fake-usr-hdr");
		} else {
			sub = request.getRemoteUser();
		}

		log.debug("Extracted sub: {}", sub);

		if (sub != null && !sub.isEmpty()) {
			log.info("Found userId: {} ", sub);
			User user = connector.getUserWithEmail(sub, extSourceProxy, userEmailAttr);
			user.setAppAdmin(applicationProperties.isAppAdmin(user.getId()));
			log.info("Found user: {}", user);

			request.getSession().setAttribute("user", user);
			return user;
		}

		return null;
	}

}
