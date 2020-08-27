package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.PersistenceUtils;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Properties;

/**
 * Configuration class of attribute inputs
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class AttrsConfig {

	private final List<AttrInput> inputs;
	private final ApplicationBeans applicationBeans;
	private final ApplicationProperties applicationProperties;

	public AttrsConfig(PerunAdapter perunAdapter,
					   Properties attrsProps,
					   AttributeCategory category,
					   ApplicationBeans applicationBeans,
					   ApplicationProperties applicationProperties)
			throws PerunUnknownException, PerunConnectionException
	{
		this.applicationBeans = applicationBeans;
		this.applicationProperties = applicationProperties;
		inputs = PersistenceUtils.initializeAttributes(perunAdapter, applicationProperties, applicationBeans,
				attrsProps, category);
	}

	List<AttrInput> getInputs() {
		return inputs;
	}

	@Override
	public String toString() {
		return "AttrsConfig{" +
				"inputs=" + inputs +
				'}';
	}
}
