package cz.metacentrum.perun.spRegistration.persistence.exceptions;

/**
 * Exception represents problem while working with Perun RPC or MitreID API
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class ConnectorException extends Exception {

	public ConnectorException() {
		super();
	}

	public ConnectorException(String s) {
		super(s);
	}

	public ConnectorException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ConnectorException(Throwable throwable) {
		super(throwable);
	}

	public ConnectorException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
