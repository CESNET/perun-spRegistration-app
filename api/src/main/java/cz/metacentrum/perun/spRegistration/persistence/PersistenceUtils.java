package cz.metacentrum.perun.spRegistration.persistence;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationBeans;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Utility class for persistence layer
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class PersistenceUtils {

	private static final Logger log = LoggerFactory.getLogger(PersistenceUtils.class);

	private final static String ATTR_NAME = "attrName";
	private final static String IS_DISPLAYED = "isDisplayed";
	private final static String IS_EDITABLE = "isEditable";
	private final static String IS_REQUIRED = "isRequired";
	private final static String ALLOWED_VALUES = "allowedValues";
	private final static String ALLOWED_KEYS = "allowedKeys";
	private final static String POSITION = "position";
	private static final String REGEX = "regex" ;

	/**
	 * Initialize attributes form configuration
	 * @param connector Perun connector to obtain information about attributes
	 * @param props properties file containing translations
	 * @return List of initialized attributes
	 */
	public static List<AttrInput> initializeAttributes(PerunAdapter connector,
													   ApplicationProperties applicationProperties,
													   Map<String, PerunAttributeDefinition> definitionMap,
													   Map<String, AttributeCategory> categoryMap,
													   Properties props,
													   AttributeCategory category)
			throws PerunUnknownException, PerunConnectionException
	{
		log.trace("Initializing attribute inputs - START");
		List<AttrInput> inputs = new ArrayList<>();
		log.debug("Locales enabled: {}", applicationProperties.getLanguagesEnabled());

		for (String prop: props.stringPropertyNames()) {
			if (! prop.contains(ATTR_NAME)) {
				continue;
			}

			String baseProp = prop.replace('.' + ATTR_NAME, "");

			String attrName = props.getProperty(prop);
			log.debug("Initializing attribute: {}", attrName);

			PerunAttributeDefinition def = connector.getAttributeDefinition(attrName);
			definitionMap.put(def.getFullName(), def);
			categoryMap.put(def.getFullName(), category);

			Map<String, String> name = getTranslations(applicationProperties.getLanguagesEnabled(), "name", baseProp, props);
			Map<String, String> desc = getTranslations(applicationProperties.getLanguagesEnabled(), "desc", baseProp, props);

			AttrInput input = new AttrInput(def.getFullName(), name, desc, def.getType());

			setAdditionalOptions(props, input, prop);
			inputs.add(input);
			log.debug("Attribute {} initialized", attrName);
		}

		log.trace("Initializing attribute inputs - FINISHED");
		return inputs;
	}

	private static Map<String, String> getTranslations(Set<String> enabledLangs, String subKey, String baseProp,
													   Properties props)
	{
		Map<String, String> map = new HashMap<>();
		for (String langKey: enabledLangs) {
			String prop = baseProp + ".lang." + subKey + '.' + langKey;
			if (props.containsKey(prop)) {
				map.put(langKey, props.getProperty(prop));
			}
		}

		return map;
	}

	private static void setAdditionalOptions(Properties props, AttrInput input, String prop) {
		log.trace("setting additional options...");

		String isRequiredProp = prop.replaceAll(ATTR_NAME, IS_REQUIRED);
		if (props.containsKey(isRequiredProp)) {
			boolean val = Boolean.parseBoolean(props.getProperty(isRequiredProp));
			input.setRequired(val);
		}

		String isDisplayedProp = prop.replaceAll(ATTR_NAME, IS_DISPLAYED);
		if (props.containsKey(isDisplayedProp)) {
			boolean val = Boolean.parseBoolean(props.getProperty(isDisplayedProp));
			input.setDisplayed(val);
		}

		String isEditableProp = prop.replaceAll(ATTR_NAME, IS_EDITABLE);
		if (props.containsKey(isEditableProp)) {
			boolean val = Boolean.parseBoolean(props.getProperty(isEditableProp));
			input.setEditable(val);
		}

		String allowedValuesProp = prop.replaceAll(ATTR_NAME, ALLOWED_VALUES);
		if (props.containsKey(allowedValuesProp)) {
			String val = props.getProperty(allowedValuesProp);
			input.setAllowedValues(Arrays.asList(val.split(",")));
		}

		String allowedKeysProp = prop.replaceAll(ATTR_NAME, ALLOWED_KEYS);
		if (props.containsKey(allowedKeysProp)) {
			String val = props.getProperty(allowedKeysProp);
			input.setAllowedKeys(Arrays.asList(val.split(",")));
		}

		String positionProp = prop.replaceAll(ATTR_NAME, POSITION);
		if (props.containsKey(positionProp)) {
			int val = Integer.parseInt(props.getProperty(positionProp));
			input.setDisplayPosition(val);
		}

		String regexProp = prop.replaceAll(ATTR_NAME, REGEX);
		if (props.containsKey(regexProp)) {
			String val = props.getProperty(regexProp);
			input.setRegex(val);
		}
	}
}
