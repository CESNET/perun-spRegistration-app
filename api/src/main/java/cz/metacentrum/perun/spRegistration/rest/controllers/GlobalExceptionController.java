package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Class that handles all exceptions thrown from REST API.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@ControllerAdvice
public class GlobalExceptionController {

	@ExceptionHandler(value = UnauthorizedActionException.class)
	public ResponseEntity<Object> exception(UnauthorizedActionException exception) {
		return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
	}
}
