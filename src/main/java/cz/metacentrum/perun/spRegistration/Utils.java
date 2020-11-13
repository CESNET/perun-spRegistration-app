package cz.metacentrum.perun.spRegistration;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utils {

	public static final String GENERIC_ERROR_MSG = "Cannot complete operation, wrong parameters passed to method.";

	/**
	 * Checks if params are invalid:
	 *   - ALL - all objects are checked if not NULL,
	 *   - Classes:
	 *     - String		- checks if value is empty
	 *     - JSONArray	- checks if value is equal to JSONObject.NULL or empty
	 *     - JSONObject	- checks if value is equal to JSONObject.NULL or empty
	 *
	 * @param objects params
	 * @return TRUE if at least one param is invalid, FALSE otherwise
	 */
	public static boolean checkParamsInvalid(Object ... objects) {
		for (Object o : objects) {
			if (o == null) {
				return true;
			} else if (String.class.equals(o.getClass())) {
				String strObj = (String) o;
				if (strObj.isEmpty()) {
					return true;
				}
			} else if (ArrayNode.class.equals(o.getClass())) {
				ArrayNode jsonArray = (ArrayNode) o;
				if (jsonArray.isNull() || jsonArray.isEmpty()) {
					return false;
				}
			} else if (ObjectNode.class.equals(o.getClass())) {
				ObjectNode jsonObject = (ObjectNode) o;
				if (jsonObject.isNull() || jsonObject.isEmpty()) {
					return false;
				}
			}
		}

		return false;
	}
}
