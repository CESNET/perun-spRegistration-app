package cz.metacentrum.perun.spRegistration.persistence.exceptions;

/**
 * Exception represents that active requests exists for the given service.
 * Only one request can be active at the same time for one service.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class ActiveRequestExistsException extends Exception {

	public ActiveRequestExistsException() {
		super();
	}

	public ActiveRequestExistsException(String s) {
		super(s);
	}

	public ActiveRequestExistsException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ActiveRequestExistsException(Throwable throwable) {
		super(throwable);
	}

	protected ActiveRequestExistsException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
