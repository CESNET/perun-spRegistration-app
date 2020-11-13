package cz.metacentrum.perun.spRegistration.common.exceptions;

/**
 * Exception thrown when status of Request cannot be changed due to unmet requirements.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class CannotChangeStatusException extends Exception {

	public CannotChangeStatusException() {
		super();
	}

	public CannotChangeStatusException(String s) {
		super(s);
	}

	public CannotChangeStatusException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public CannotChangeStatusException(Throwable throwable) {
		super(throwable);
	}

	protected CannotChangeStatusException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
