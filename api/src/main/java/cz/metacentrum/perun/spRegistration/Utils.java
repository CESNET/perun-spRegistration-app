package cz.metacentrum.perun.spRegistration;

import org.json.JSONArray;
import org.json.JSONObject;

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
			} else if (JSONArray.class.equals(o.getClass())) {
				JSONArray jsonArray = (JSONArray) o;
				if (JSONObject.NULL.equals(jsonArray) || jsonArray.isEmpty()) {
					return false;
				}
			} else if (JSONObject.class.equals(o.getClass())) {
				JSONObject jsonObject = (JSONObject) o;
				if (JSONObject.NULL.equals(jsonObject) || jsonObject.isEmpty()) {
					return false;
				}
			}
		}

		return false;
	}
}
