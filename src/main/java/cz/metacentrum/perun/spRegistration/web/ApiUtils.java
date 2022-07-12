package cz.metacentrum.perun.spRegistration.web;

import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiUtils {

	public static String normalizeRequestBodyString(@NonNull String code) {
		if (code.startsWith("\"")) {
			code = code.substring(1, code.length() - 1);
		}

		log.trace("normalizeRequestBodyString() returns: {}", code);
		return code;
	}

	public static boolean isAppAdmin() {
		return ServiceUtils.isAppAdmin();
	}

}
