package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

/**
 * Class handles all exceptions thrown from REST API.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionController {

	@ExceptionHandler(value = { InternalErrorException.class, BadPaddingException.class,
			UnsupportedEncodingException.class, InvalidKeyException.class, IllegalBlockSizeException.class,
			BadPaddingException.class, NullPointerException.class })
	public ResponseEntity<Object> internalErrorException(Exception exception) {
		log.error("{}", exception.getMessage(), exception);
		return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { ExpiredCodeException.class, CodeNotStoredException.class } )
	public ResponseEntity<Object> forbidden(Exception exception) {
		log.error("{}", exception.getMessage(), exception);
		return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(value = UnauthorizedActionException.class)
	public ResponseEntity<Object> unauthorized(Exception exception) {
		log.error("{}", exception.getMessage(), exception);
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public ResponseEntity<Object> illegalArgumentException(Exception exception) {
		log.error("{}", exception.getMessage(), exception);
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = CannotChangeStatusException.class)
	public ResponseEntity<Object> cannotChangeStatusException(Exception exception) {
		log.error("{}", exception.getMessage(), exception);
		return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
	}
}
