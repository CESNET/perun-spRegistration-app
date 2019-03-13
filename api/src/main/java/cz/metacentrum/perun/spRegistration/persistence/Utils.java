package cz.metacentrum.perun.spRegistration.persistence;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.Mails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	private final static String ATTR_NAME = "attrName";
	private final static String IS_DISPLAYED = "isDisplayed";
	private final static String IS_EDITABLE = "isEditable";
	private final static String IS_REQUIRED = "isRequired";
	private final static String ALLOWED_VALUES = "allowedValues";
	private final static String ALLOWED_KEYS = "allowedKeys";
	private final static String POSITION = "position";
	private static final String REGEX = "regex" ;

	public static List<AttrInput> initializeAttributes(PerunConnector connector, AppConfig appConfig, Properties props) throws RPCException {
		log.debug("Initializing attribute inputs - START");
		List<AttrInput> inputs = new ArrayList<>();
		Properties en = appConfig.getEnLocale();
		Properties cs = appConfig.getCsLocale();
		boolean isCsDisabled = cs.isEmpty();
		log.debug("Locales enabled: EN - {}, CS - {}", true, !isCsDisabled);

		for (String prop: props.stringPropertyNames()) {
			if (! prop.contains(ATTR_NAME)) {
				continue;
			}

			String attrName = props.getProperty(prop);
			log.debug("Initializing attribute: {}", attrName);

			PerunAttributeDefinition def = connector.getAttributeDefinition(attrName);
			appConfig.getPerunAttributeDefinitionsMap().put(def.getFullName(), def);

			Map<String, String> name = new HashMap<>();
			Map<String, String> desc = new HashMap<>();

			String nameKey = attrName.replaceAll(":", ".") + ".name";
			String descKey = attrName.replaceAll(":", ".") + ".desc";

			name.put("en", en.getProperty(nameKey));
			desc.put("en", en.getProperty(descKey));

			if (! isCsDisabled) {
				name.put("cs", cs.getProperty(nameKey));
				desc.put("cs", cs.getProperty(descKey));
			}

			AttrInput input = new AttrInput(def.getFullName(), name, desc, def.getType());

			setAdditionalOptions(props, input, prop);
			log.debug("Attribute {} initialized", attrName);
			inputs.add(input);
		}

		log.debug("Initializing attribute inputs - FINISHED");
		return inputs;
	}

	private static void setAdditionalOptions(Properties props, AttrInput input, String prop) {
		log.debug("setting additional options...");
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

	public static boolean updaterequestAndNotifyUser(RequestManager rm, Request request, RequestStatus newStatus,
													 Properties mp, String adminsAttr) {
		boolean res = updateRequest(rm, request, newStatus);
		if (res) {
			res = updateRequestInDbAndNotifyUser(mp, adminsAttr, request);
		}

		return res;
	}

	private static boolean updateRequest(RequestManager rm, Request request, RequestStatus newStatus) {
//		if (requestId != null) {
//			request.setReqId(requestId);
//		}
//		if (modifiedById != null) {
//			request.setModifiedBy(modifiedById);
//		}
		request.setStatus(newStatus);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("updating request in DB");
		boolean res = rm.updateRequest(request);

		log.debug("updateRequest returns: {}", res);
		return res;
	}

	/**
	 * Update request in Database and notify user about update
	 * @param mp MessageProperties instance
	 * @param adminsAttr attribute where admins are stored
	 * @param request request
	 * @return TRUE if all went OK in DB false otherwise
	 */
	private static boolean updateRequestInDbAndNotifyUser(Properties mp, String adminsAttr, Request request) {
		log.debug("updateRequestInDbAndNotifyUser(mp: {}, adminsAttr: {}, request: {})",
				mp, adminsAttr, request);

		if (request == null) {
			throw new IllegalArgumentException("Request cannot be null");
		}

		log.debug("sending mail notification");
		boolean res = Mails.requestStatusUpdateUserNotify(request.getReqId(), request.getStatus(), request.getAdmins(adminsAttr), mp);

		log.debug("updateRequestInDbAndNotifyUser() returns: {}", res);
		return res;
	}
}
