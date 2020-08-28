package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.models.User;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthController {

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

	@GetMapping(path = "/api/unsetUser")
	public boolean unsetUser(HttpServletRequest req) {
		HttpSession sess = req.getSession();
		if (sess != null && sess.getAttribute("user") != null) {
			sess.setAttribute("user", null);
		}

		return true;
	}

}
