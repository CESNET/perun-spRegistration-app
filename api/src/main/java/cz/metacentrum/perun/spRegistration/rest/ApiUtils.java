package cz.metacentrum.perun.spRegistration.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ApiUtils {

	public static String decodeCode(String code) throws UnsupportedEncodingException {
		if (code.startsWith("\"")) {
			code = code.substring(1, code.length() - 1);
		}

		return URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
	}
}
