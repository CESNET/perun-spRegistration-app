package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.PersistenceUtils;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration class of attribute inputs
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter(value = AccessLevel.PACKAGE)
@ToString
public class AttrsConfig {

	private final List<AttrInput> inputs;

	public AttrsConfig(PerunAdapter perunAdapter,
					   List<AttrInput> inputs,
					   AttributeCategory category,
					   Map<String, PerunAttributeDefinition> definitionMap,
					   Map<String, AttributeCategory> categoryMap,
					   ApplicationProperties applicationProperties)
			throws PerunUnknownException, PerunConnectionException
	{
		this.inputs = inputs;
		PersistenceUtils.initializeAttributes(
				perunAdapter,
				applicationProperties,
				definitionMap,
				categoryMap,
				inputs,
				category
		);
	}

}
