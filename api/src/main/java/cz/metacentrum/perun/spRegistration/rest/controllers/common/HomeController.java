package cz.metacentrum.perun.spRegistration.rest.controllers.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@Controller
public class HomeController {

	@GetMapping(path = "/home")
	public String getLoggedUser() {
		return "forward:/index.html";
	}
}
