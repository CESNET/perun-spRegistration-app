package cz.metacentrum.perun.spRegistration.persistence.configs;

import cz.metacentrum.perun.spRegistration.persistence.PersistenceUtils;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;

import java.util.List;
import java.util.Properties;

/**
 * Configuration class of attribute inputs
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class AttrsConfig {

	private List<AttrInput> inputs;

	public AttrsConfig(AppConfig appConfig, PerunConnector perunConnector, Properties attrsProps) throws ConnectorException {
		inputs = PersistenceUtils.initializeAttributes(perunConnector, appConfig, attrsProps);
	}

	List<AttrInput> getInputs() {
		return inputs;
	}

	void setInputs(List<AttrInput> inputs) {
		this.inputs = inputs;
	}

	@Override
	public String toString() {
		return "AttrsConfig{" +
				"inputs=" + inputs +
				'}';
	}
}
