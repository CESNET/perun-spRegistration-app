package cz.metacentrum.perun.spRegistration.rest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouteToAngular {

	@RequestMapping(path = { "/", "", "/auth", "/auth/**" })
	public String forwardToAngular() {
		return "forward:/index.html";
	}
}
