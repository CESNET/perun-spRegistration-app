package cz.metacentrum.perun.spRegistration.persistence;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Utility class for persistence layer
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class PersistenceUtils {

	public static void initializeAttributes(@NonNull PerunAdapter connector,
											@NonNull ApplicationProperties applicationProperties,
											@NonNull Map<String, PerunAttributeDefinition> definitionMap,
											@NonNull Map<String, AttributeCategory> categoryMap,
											@NonNull List<AttrInput> inputs,
											@NonNull AttributeCategory category)
			throws PerunUnknownException, PerunConnectionException
	{
		log.trace("Initializing attribute inputs - START");
		log.debug("Locales enabled: {}", applicationProperties.getLanguagesEnabled());

		for (AttrInput input: inputs) {
			String attrName = input.getName();
			log.debug("Initializing attribute: {}", attrName);

			PerunAttributeDefinition def = connector.getAttributeDefinition(attrName);
			definitionMap.put(def.getFullName(), def);
			categoryMap.put(def.getFullName(), category);

			log.debug("Attribute {} initialized", attrName);
		}

		log.trace("Initializing attributes - FINISHED");
	}

}
