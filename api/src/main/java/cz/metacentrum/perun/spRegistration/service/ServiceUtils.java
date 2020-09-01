package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class containing methods for services.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class ServiceUtils {

	public static final String CIPHER_PARAMS = "AES/ECB/PKCS5PADDING";
	public static Cipher cipher;

	static {
		try {
			cipher = Cipher.getInstance(CIPHER_PARAMS);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transform list of facilities into Map, where key is ID, value is Facility object.
	 *
	 * @param list List to be transformed
	 * @return NULL when input is NULL, empty Map when input is empty, List converted to Map otherwise.
	 */
	public static Map<Long, Facility> transformListToMapFacilities(List<Facility> list) {
		if (list == null) {
			return null;
		} else if (list.isEmpty()) {
			return new HashMap<>();
		} else {
			return list.stream().collect(Collectors.toMap(Facility::getId, facility -> facility));
		}
	}

	/**
	 * Filter facility attributes - keep only ones with name in list.
	 * @param attrsMap Map of attributes to be filtered.
	 * @param toKeep Names of attributes that should be kept.
	 * @return NULL if param "attrsMap" or "toKeep" is NULL, empty map if param "attrsMap" or "toKeep" is empty,
	 * Filtered map otherwise.
	 */
	public static List<PerunAttribute> filterFacilityAttrs(Map<String, PerunAttribute> attrsMap, List<String> toKeep) {
		log.trace("filterFacilityAttrs(attrsMap: {}, toKeep: {})", attrsMap, toKeep);
		if (attrsMap == null) {
			return null;
		} else if (attrsMap.isEmpty()) {
			return new ArrayList<>();
		} else if (toKeep == null) {
			return null;
		} else if (toKeep.isEmpty()) {
			return new ArrayList<>();
		}

		List<PerunAttribute> filteredAttributes = new ArrayList<>();
		for (String keptAttr: toKeep) {
			filteredAttributes.add(attrsMap.get(keptAttr));
		}

		log.trace("filterFacilityAttrs() returns: {}", filteredAttributes);
		return filteredAttributes;
	}

	/**
	 * Decide if request is for OIDC service.
	 * @param request Request
	 * @param entityIdAttr Identifier of entity id attr.
	 * @return True if is OIDC service request, false otherwise.
	 */
	public static boolean isOidcRequest(Request request, String entityIdAttr) {
		log.trace("isOidcRequest(request: {})", request);
		boolean isOidc = true;
		if (request.getAttributes().get(AttributeCategory.PROTOCOL).containsKey(entityIdAttr)) {
			isOidc = (null == request.getAttributes().get(AttributeCategory.PROTOCOL).get(entityIdAttr).getValue());
		}

		log.trace("isOidcRequest() returns: {}", isOidc);
		return isOidc;
	}

	/**
	 * Decide if attributes are of OIDC service.
	 * @param attributes Map of attributes, key is name, value is attribute
	 * @param entityIdAttr Identifier of entity id attr.
	 * @return True if attributes are of OIDC service, false otherwise.
	 */
	public static boolean isOidcAttributes(Map<String, PerunAttribute> attributes, String entityIdAttr) {
		log.trace("isOidcAttributes(attributes: {})", attributes);
		boolean isOidc = true;

		if (attributes.containsKey(entityIdAttr)) {
			isOidc = (null == attributes.get(entityIdAttr).getValue());
		}

		log.trace("isOidcAttributes() returns: {}", isOidc);
		return isOidc;
	}

	public static String encrypt(String strToEncrypt, SecretKeySpec secretKeySpec)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		Base64.Encoder b64enc = Base64.getUrlEncoder();

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

		return b64enc.encodeToString(encrypted);
	}

	public static String decrypt(String strToDecrypt, SecretKeySpec secretKeySpec) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		if (strToDecrypt == null || strToDecrypt.equals("null")) {
			return null;
		}

		Base64.Decoder b64dec = Base64.getUrlDecoder();

		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] decrypted = cipher.doFinal(b64dec.decode(strToDecrypt));

		return new String(decrypted);
	}

	public static String generateClientId() {
		return UUID.randomUUID().toString();
	}

	public static String generateClientSecret() {
		String uuid = UUID.randomUUID().toString();
		uuid += UUID.randomUUID().toString();

		return uuid;
	}

	public static String getHash(String input) {
		byte[] digest = DigestUtils.md5Digest(input.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : digest) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString().toUpperCase();
	}

	public static Map<String, String> extractFacilityName(Facility facility, AttributesProperties attrsProps) {
		return ServiceUtils.extractFacilityName(facility.getAttributes().get(AttributeCategory.SERVICE),
				attrsProps);
	}

	public static Map<String, String> extractFacilityDescription(Facility facility, AttributesProperties attrsProps) {
		return ServiceUtils.extractFacilityDescription(facility.getAttributes().get(AttributeCategory.SERVICE),
				attrsProps);
	}

	public static Map<String, String> extractFacilityName(Map<String, PerunAttribute> attrs,
														  AttributesProperties attributesProperties)
	{
		return attrs.get(attributesProperties.getServiceNameAttrName()).valueAsMap();
	}

	public static Map<String, String> extractFacilityDescription(Map<String, PerunAttribute> attrs,
																 AttributesProperties attributesProperties)
	{
		return attrs.get(attributesProperties.getServiceDescAttrName()).valueAsMap();
	}

}
