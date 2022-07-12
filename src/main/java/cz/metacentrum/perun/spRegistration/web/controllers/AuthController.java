package cz.metacentrum.perun.spRegistration.web.controllers;

import cz.metacentrum.perun.spRegistration.common.models.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@RestController
@Slf4j
public class AuthController {

	public static final String SESS_ATTR_USER = "user";

	@GetMapping(path = "/api/getUser")
	public User getUser(HttpServletRequest req) {
		HttpSession sess = req.getSession();
		if (sess == null || sess.getAttribute(SESS_ATTR_USER) == null) {
			return null;
		}
		return (User) sess.getAttribute(SESS_ATTR_USER);
	}

	@GetMapping(path = "/logout")
	public String logout(HttpSession sess) {
		sess.removeAttribute(SESS_ATTR_USER);
		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
		return "redirect:/";
	}


}
