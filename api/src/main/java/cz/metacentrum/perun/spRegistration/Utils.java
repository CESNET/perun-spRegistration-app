package cz.metacentrum.perun.spRegistration;

public class Utils {

	public static final String GENERIC_ERROR_MSG = "Cannot complete operation, wrong parameters passed to method.";

	/**
	 * Checks if params are invalid:
	 * String - checks if null or empty
	 * Other - checks for null
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
			}
		}

		return false;
	}
}
