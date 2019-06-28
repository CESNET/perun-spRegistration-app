package cz.metacentrum.perun.spRegistration.persistence.enums;

/**
 * Status of the request.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public enum RequestStatus {

	APPROVED,
	REJECTED,
	WAITING_FOR_APPROVAL,
	WAITING_FOR_CHANGES;

	/**
	 * Convert Enum value to INTEGER representation (used in DB).
	 * @return Positive Int value [1-4], -1 in case of error.
	 */
	public int getAsInt() {
		switch (this) {
			case APPROVED: return 1;
			case REJECTED: return 2;
			case WAITING_FOR_APPROVAL: return 3;
			case WAITING_FOR_CHANGES: return 4;
			default: return -1;
		}
	}

	/**
	 * Convert INTEGER value (from DB) to ENUM value.
	 * @param i value to be converted (accepted values are in range [1-4])
	 * @return Enum value, null n case of error.
	 */
	public static RequestStatus resolve(int i) {
		switch (i) {
			case 1: return APPROVED;
			case 2: return REJECTED;
			case 3: return WAITING_FOR_APPROVAL;
			case 4: return WAITING_FOR_CHANGES;
			default: return null;
		}
	}
}
