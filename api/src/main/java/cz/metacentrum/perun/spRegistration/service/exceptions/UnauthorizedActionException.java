package cz.metacentrum.perun.spRegistration.service.exceptions;

/**
 * Exception thrown when user tries to perform action he/she is not authorized to perform.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class UnauthorizedActionException extends Exception {

	public UnauthorizedActionException() {
		super();
	}

	public UnauthorizedActionException(String s) {
		super(s);
	}

	public UnauthorizedActionException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public UnauthorizedActionException(Throwable throwable) {
		super(throwable);
	}

	protected UnauthorizedActionException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
