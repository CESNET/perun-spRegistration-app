package cz.metacentrum.perun.spRegistration.rest.interceptors;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class UserSettingInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(UserSettingInterceptor.class);

	private final AppConfig appConfig;
	private final PerunConnector connector;

	@Value("${dev.enabled}")
	private boolean devEnabled;

	@Autowired
	public UserSettingInterceptor(AppConfig appConfig, PerunConnector connector) {
		this.appConfig = appConfig;
		this.connector = connector;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.debug("UserSettingInterceptor 'preHandle()'");

		User userFromRequest = (User)request.getSession().getAttribute("user");

		if (userFromRequest == null && setUser(request) == null) {
			String url = request.getRequestURL().toString();
			int index = url.indexOf("/spreg/");
			url = url.substring(0, index);
			response.sendRedirect(url + "/spreg/");
			return false;
		}

		return true;
	}

	private User setUser(HttpServletRequest request) throws ConnectorException {
		String userEmailAttr = appConfig.getUserEmailAttributeName();
		String extSourceProxy = appConfig.getLoginExtSource();
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
			user.setAdmin(appConfig.isAppAdmin(user.getId()));
			log.info("Found user: {}", user);

			request.getSession().setAttribute("user", user);
			return user;
		}

		return null;
	}
}
