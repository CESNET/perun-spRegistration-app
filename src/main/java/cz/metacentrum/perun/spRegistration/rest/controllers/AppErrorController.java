package cz.metacentrum.perun.spRegistration.rest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller handling errors
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@Controller
@Slf4j
public class AppErrorController implements ErrorController {

	public static final String PATH = "/error";

	@GetMapping(value = PATH)
	public String handleError(HttpServletRequest request) {
		log.trace("handleError({})", request.getRequestURL());
		return "forward:index.html";
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}

}