package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.PersistenceUtils;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration class of attribute inputs
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class AttrsConfig {

	private final List<AttrInput> inputs;

	public AttrsConfig(PerunAdapter perunAdapter,
					   Properties attrsProps,
					   AttributeCategory category,
					   Map<String, PerunAttributeDefinition> definitionMap,
					   Map<String, AttributeCategory> categoryMap,
					   ApplicationProperties applicationProperties)
			throws PerunUnknownException, PerunConnectionException
	{
		inputs = PersistenceUtils.initializeAttributes(
				perunAdapter,
				applicationProperties,
				definitionMap,
				categoryMap,
				attrsProps,
				category
		);
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
