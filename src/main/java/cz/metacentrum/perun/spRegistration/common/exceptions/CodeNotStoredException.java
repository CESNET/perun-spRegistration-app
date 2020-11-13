package cz.metacentrum.perun.spRegistration.common.exceptions;

/**
 * Throw when given code is valid but not found in DB (meaning probably already used)
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class CodeNotStoredException extends Exception {

	public CodeNotStoredException() {
		super();
	}

	public CodeNotStoredException(String s) {
		super(s);
	}

	public CodeNotStoredException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public CodeNotStoredException(Throwable throwable) {
		super(throwable);
	}

	protected CodeNotStoredException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
