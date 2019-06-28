package cz.metacentrum.perun.spRegistration.service.exceptions;

/**
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class ExpiredCodeException extends Exception {

	public ExpiredCodeException() {
		super();
	}

	public ExpiredCodeException(String s) {
		super(s);
	}

	public ExpiredCodeException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ExpiredCodeException(Throwable throwable) {
		super(throwable);
	}

	protected ExpiredCodeException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
