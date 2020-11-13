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

	@GetMapping(value = "/error")
	public String handleError(HttpServletRequest request) {
		log.trace("handleError({})", request.getRequestURL());
		return "/resources/static/index.html";
	}

	@Override
	public String getErrorPath() {
		log.trace("getErrorPath()");
		return "/error";
	}
}