package cz.metacentrum.perun.spRegistration.common.exceptions;

/**
 * Exception thrown when some internal error (database inconsistency...) occurs.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class InternalErrorException extends Exception {

	public InternalErrorException() {
		super();
	}

	public InternalErrorException(String s) {
		super(s);
	}

	public InternalErrorException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public InternalErrorException(Throwable throwable) {
		super(throwable);
	}

	protected InternalErrorException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
