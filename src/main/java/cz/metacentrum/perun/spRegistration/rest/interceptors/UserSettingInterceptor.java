package cz.metacentrum.perun.spRegistration.rest.interceptors;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@Slf4j
public class UserSettingInterceptor implements HandlerInterceptor {

	public static final String FAKE_USER_HEADER = "fake-usr-hdr";
	public static final String SESSION_USER = "user";

	@Value("${dev.enabled:false}")
	private boolean devEnabled;

	@NonNull private final PerunAdapter connector;
	@NonNull private final AttributesProperties attributesProperties;
	@NonNull private final ApplicationProperties applicationProperties;

	public UserSettingInterceptor(@NonNull PerunAdapter connector,
								  @NonNull AttributesProperties attributesProperties,
								  @NonNull ApplicationProperties applicationProperties)
	{
		this.connector = connector;
		this.attributesProperties = attributesProperties;
		this.applicationProperties = applicationProperties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception
	{
		if (request.getSession().getAttribute(SESSION_USER) == null
				&& setUser(request) == null) {
			String url = request.getRequestURL().toString();
			int index = url.indexOf("/spreg/");
			url = url.substring(0, index);
			response.sendRedirect(url + "/spreg/");
			return false;
		}

		return true;
	}

	private User setUser(HttpServletRequest request) throws PerunUnknownException, PerunConnectionException {
		String userEmailAttr = attributesProperties.getNames().getUserEmail();
		String extSourceProxy = applicationProperties.getProxyIdentifier();
		String sub;

		if (devEnabled) {
			sub = request.getHeader(FAKE_USER_HEADER);
		} else {
			sub = request.getRemoteUser();
		}
		if (sub != null && !sub.isEmpty()) {
			User user = connector.getUserWithEmail(sub, extSourceProxy, userEmailAttr);
			if (user == null) {
				return null;
			}
			user.setAppAdmin(applicationProperties.isAppAdmin(user.getId()));
			request.getSession().setAttribute(SESSION_USER, user);
			return user;
		}

		return null;
	}

}
