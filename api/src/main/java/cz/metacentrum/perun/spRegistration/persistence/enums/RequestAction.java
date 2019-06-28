package cz.metacentrum.perun.spRegistration.persistence.enums;

/**
 * Action associated with request.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public enum RequestAction {

	REGISTER_NEW_SP,
	UPDATE_FACILITY,
	DELETE_FACILITY,
	MOVE_TO_PRODUCTION;

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
		}

		return null;
	}
}
