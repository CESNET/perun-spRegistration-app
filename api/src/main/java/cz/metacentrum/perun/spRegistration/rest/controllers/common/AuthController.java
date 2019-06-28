package cz.metacentrum.perun.spRegistration.rest.controllers.common;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Controller handling authentication
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AppConfig appConfig;
	private final PerunConnector connector;

	@Value("${dev.enabled}")
	private boolean devEnabled;

	@Autowired
	public AuthController(AppConfig appConfig, PerunConnector connector) {
		this.appConfig = appConfig;
		this.connector = connector;
	}

	@GetMapping(path = "/api/getUser")
	public User getUser(HttpServletRequest req) {
		HttpSession sess = req.getSession();
		User user = null;

		if (sess != null && sess.getAttribute("user") != null) {
			user = (User) sess.getAttribute("user");
		}

		log.trace("getUser() returns: {}", user);
		return user;
	}

}
