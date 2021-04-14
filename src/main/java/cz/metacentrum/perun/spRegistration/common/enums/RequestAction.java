package cz.metacentrum.perun.spRegistration.common.enums;

/**
 * Action associated with request.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public enum RequestAction {

	REGISTER_NEW_SP,
	UPDATE_FACILITY,
	DELETE_FACILITY,
	MOVE_TO_PRODUCTION,
	UNKNOWN;

	/**
	 * Convert Enum value to INTEGER representation (used in DB).
	 * @return Positive Int value [1-5], -1 if cannot be parsed.
	 */
	public int getAsInt() {
		switch (this) {
			case REGISTER_NEW_SP: return 1;
			case UPDATE_FACILITY: return 2;
			case DELETE_FACILITY:  return 3;
			case MOVE_TO_PRODUCTION: return 4;
		}

		return -1;
	}

	/**
	 * Convert INTEGER value (from DB) to ENUM value.
	 * @param i value to be converted (accepted values are in range [1-5])
	 * @return Enum value, null if cannot be parsed.
	 */
	public static RequestAction resolve(int i) {
		switch (i) {
			case 1: return REGISTER_NEW_SP;
			case 2: return UPDATE_FACILITY;
			case 3: return DELETE_FACILITY;
			case 4: return MOVE_TO_PRODUCTION;
			default: return UNKNOWN;
		}
	}


	@Override
	public String toString() {
		return super.toString();
	}

	public String toString(String lang) {
		switch (this) {
				case REGISTER_NEW_SP: return "cs".equalsIgnoreCase(lang) ? "Registrace služby" : "Register new service";
				case UPDATE_FACILITY: return "cs".equalsIgnoreCase(lang) ? "Změna konfigurace" : "Update configuration";
				case DELETE_FACILITY: return "cs".equalsIgnoreCase(lang) ? "Zmazání služby" : "Remove service";
				case MOVE_TO_PRODUCTION: return "cs".equalsIgnoreCase(lang) ? "Převod do produkce" : "Transfer to production";
				default: return this.toString();
		}
	}

}
