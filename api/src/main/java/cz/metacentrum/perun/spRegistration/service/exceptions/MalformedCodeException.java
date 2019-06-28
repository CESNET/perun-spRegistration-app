package cz.metacentrum.perun.spRegistration.service.exceptions;

/**
 * Exception represents malformed format of the validation code for signing requests / approving administrators.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class MalformedCodeException extends Exception {

	public MalformedCodeException() {
		super();
	}

	public MalformedCodeException(String s) {
		super(s);
	}

	public MalformedCodeException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public MalformedCodeException(Throwable throwable) {
		super(throwable);
	}

	protected MalformedCodeException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
